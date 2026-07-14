package com.review.agent.infrastructure.webhook;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.GitLabMergeRequestNoteResultVO;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.service.ReviewService;
import com.review.agent.service.GitLabMergeRequestNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.PatternMatchUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabMergeRequestReviewTrigger {

    private static final String CONNECTOR_KEY = "gitlab-merge-request";
    private static final Set<String> REVIEWABLE_ACTIONS = Set.of("open", "reopen", "update");

    private final ObjectMapper objectMapper;
    private final ProjectGitLabConfigMapper gitLabConfigMapper;
    private final IntegrationWebhookReviewTriggerRepository triggerRepository;
    private final ReviewService reviewService;
    private final GitLabMergeRequestNoteService mergeRequestNoteService;

    public GitLabMergeRequestReviewTriggerResult trigger(String eventType, String payload) {
        String triggerKey = null;
        boolean reserved = false;
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!isMergeRequestEvent(eventType, root)) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Event is not a GitLab merge request");
            }

            JsonNode attributes = root.path("object_attributes");
            String action = attributes.path("action").asText("").toLowerCase();
            String state = attributes.path("state").asText("").toLowerCase();
            if (!REVIEWABLE_ACTIONS.contains(action) || !"opened".equals(state)) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Merge request action is not reviewable: " + action + "/" + state);
            }

            String projectPath = root.path("project").path("path_with_namespace").asText("");
            ProjectGitLabConfig config = findProjectConfig(projectPath);
            if (config == null) {
                return GitLabMergeRequestReviewTriggerResult.skipped("No enabled Review Agent project matches GitLab project: " + projectPath);
            }
            if (Boolean.FALSE.equals(config.getAutoReviewEnabled())) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Automatic GitLab MR review is disabled for this project");
            }
            if (!Boolean.TRUE.equals(config.getReviewDrafts()) && isDraft(attributes)) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Draft or WIP merge requests are excluded by project policy");
            }

            String sourceBranch = attributes.path("source_branch").asText("");
            String targetBranch = attributes.path("target_branch").asText("");
            if (sourceBranch.isBlank() || targetBranch.isBlank()) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Merge request source or target branch is missing");
            }
            if (!matchesTargetBranch(config.getTargetBranchPattern(), targetBranch)) {
                return GitLabMergeRequestReviewTriggerResult.skipped(
                        "Target branch does not match project review policy: " + targetBranch);
            }

            String mergeRequestIid = attributes.path("iid").asText("");
            String commitSha = attributes.path("last_commit").path("id").asText("");
            if (mergeRequestIid.isBlank() || commitSha.isBlank()) {
                return GitLabMergeRequestReviewTriggerResult.skipped("Merge request iid or last commit is missing");
            }
            triggerKey = projectPath + ":mr:" + mergeRequestIid + ":commit:" + commitSha;
            reserved = triggerRepository.tryReserve(
                    CONNECTOR_KEY,
                    triggerKey,
                    config.getProjectId(),
                    sourceBranch,
                    targetBranch,
                    mergeRequestIid,
                    attributes.path("url").asText(null));
            if (!reserved) {
                IntegrationWebhookReviewTrigger existing = triggerRepository.find(CONNECTOR_KEY, triggerKey).orElse(null);
                Long existingReviewId = existing == null ? null : existing.getReviewId();
                String existingStatus = existing == null ? "UNKNOWN" : existing.getTriggerStatus();
                return GitLabMergeRequestReviewTriggerResult.deduplicated(
                        triggerKey,
                        existingReviewId,
                        "Review trigger already exists with status " + existingStatus);
            }

            CreatePrePrRequest request = new CreatePrePrRequest();
            request.setProjectId(config.getProjectId());
            request.setSourceBranch(sourceBranch);
            request.setTargetBranch(targetBranch);
            ReviewDetailVO detail = reviewService.createPrePrReview(request);
            Long reviewId = detail == null || detail.getReview() == null ? null : detail.getReview().getId();
            if (reviewId == null) {
                String message = "Pre-PR review was created without a review id";
                LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(1);
                triggerRepository.markFailed(CONNECTOR_KEY, triggerKey, message, nextRetryAt);
                return GitLabMergeRequestReviewTriggerResult.failed(triggerKey, message, 1, nextRetryAt);
            }
            triggerRepository.markProcessed(CONNECTOR_KEY, triggerKey, reviewId);
            String completionMessage = null;
            if (Boolean.TRUE.equals(config.getPublishSummaryEnabled())) {
                try {
                    GitLabMergeRequestNoteResultVO noteResult = mergeRequestNoteService.postAutoSummary(
                            reviewId, mergeRequestIid);
                    completionMessage = "GitLab MR summary: " + noteResult.getStatus()
                            + (noteResult.getMessage() == null ? "" : " - " + noteResult.getMessage());
                } catch (Exception noteError) {
                    completionMessage = "Review completed; GitLab MR summary failed: " + safeMessage(noteError);
                    log.warn("Review {} completed but GitLab MR summary failed", reviewId, noteError);
                }
            }
            return GitLabMergeRequestReviewTriggerResult.processed(triggerKey, reviewId, completionMessage);
        } catch (Exception ex) {
            log.warn("Failed to trigger Pre-PR review from GitLab webhook", ex);
            String message = safeMessage(ex);
            if (reserved && triggerKey != null) {
                LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(1);
                triggerRepository.markFailed(CONNECTOR_KEY, triggerKey, message, nextRetryAt);
                return GitLabMergeRequestReviewTriggerResult.failed(triggerKey, message, 1, nextRetryAt);
            }
            return GitLabMergeRequestReviewTriggerResult.failed(triggerKey, message);
        }
    }

    private boolean isMergeRequestEvent(String eventType, JsonNode root) {
        return "merge_request".equalsIgnoreCase(root.path("object_kind").asText())
                || "Merge Request Hook".equalsIgnoreCase(eventType);
    }

    private ProjectGitLabConfig findProjectConfig(String projectPath) {
        if (projectPath == null || projectPath.isBlank()) {
            return null;
        }
        String encodedPath = URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
        return gitLabConfigMapper.selectOne(new LambdaQueryWrapper<ProjectGitLabConfig>()
                .eq(ProjectGitLabConfig::getProjectPath, encodedPath)
                .eq(ProjectGitLabConfig::getEnabled, true));
    }

    private boolean isDraft(JsonNode attributes) {
        String title = attributes.path("title").asText("").trim().toLowerCase();
        return attributes.path("draft").asBoolean(false)
                || attributes.path("work_in_progress").asBoolean(false)
                || title.startsWith("draft:")
                || title.startsWith("wip:");
    }

    private boolean matchesTargetBranch(String configuredPatterns, String targetBranch) {
        if (configuredPatterns == null || configuredPatterns.isBlank()) {
            return true;
        }
        for (String pattern : configuredPatterns.split("[,\\r\\n]+")) {
            if (!pattern.isBlank() && PatternMatchUtils.simpleMatch(pattern.trim(), targetBranch)) {
                return true;
            }
        }
        return false;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
