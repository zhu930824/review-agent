package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.GitLabMergeRequestNoteRequest;
import com.review.agent.domain.dto.GitLabMergeRequestNoteResultVO;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.infrastructure.git.GitLabMergeRequestNoteClient;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.service.GitLabMergeRequestNoteService;
import com.review.agent.service.ReviewService;
import com.review.agent.service.ReviewSummaryMarkdownBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabMergeRequestNoteServiceImpl implements GitLabMergeRequestNoteService {

    private static final String CONNECTOR_KEY = "gitlab-merge-request";

    private final ReviewService reviewService;
    private final ProjectGitLabConfigMapper configMapper;
    private final IntegrationWebhookReviewTriggerRepository triggerRepository;
    private final IntegrationActionLogRepository actionLogRepository;
    private final GitLabMergeRequestNoteClient client;
    private final ReviewSummaryMarkdownBuilder summaryBuilder;

    @Override
    public GitLabMergeRequestNoteResultVO postAutoSummary(Long reviewId, String mergeRequestIid) {
        ReviewDetailVO detail = reviewService.getReviewDetail(reviewId);
        GitLabMergeRequestNoteRequest request = new GitLabMergeRequestNoteRequest();
        request.setReviewId(reviewId);
        request.setMergeRequestIid(mergeRequestIid);
        request.setBody(summaryBuilder.build(detail));
        return post(request, detail);
    }

    @Override
    public GitLabMergeRequestNoteResultVO post(GitLabMergeRequestNoteRequest request) {
        ReviewDetailVO detail = reviewService.getReviewDetail(request.getReviewId());
        return post(request, detail);
    }

    private GitLabMergeRequestNoteResultVO post(
            GitLabMergeRequestNoteRequest request,
            ReviewDetailVO detail) {
        if (detail == null || detail.getReview() == null) {
            throw new IllegalArgumentException("Review not found: " + request.getReviewId());
        }
        Long projectId = detail.getReview().getProjectId();
        ProjectGitLabConfig config = configMapper.selectOne(new LambdaQueryWrapper<ProjectGitLabConfig>()
                .eq(ProjectGitLabConfig::getProjectId, projectId)
                .eq(ProjectGitLabConfig::getEnabled, true));
        String mergeRequestIid = resolveMergeRequestIid(request, request.getReviewId());
        if (config == null || config.getGitlabToken() == null || config.getGitlabToken().isBlank()
                || mergeRequestIid == null || mergeRequestIid.isBlank()) {
            safeRecord("SKIPPED", request.getReviewId(), mergeRequestIid, null,
                    "GitLab project config or merge request iid is missing");
            return result("SKIPPED", "GitLab project config or merge request iid is missing", mergeRequestIid, null);
        }

        String requestUrl = client.requestUrl(config, mergeRequestIid);
        try {
            client.post(config, mergeRequestIid, request.getBody());
            safeRecord("POSTED", request.getReviewId(), mergeRequestIid, requestUrl, null);
            return result("POSTED", "GitLab merge request summary posted", mergeRequestIid, requestUrl);
        } catch (Exception ex) {
            safeRecord("FAILED", request.getReviewId(), mergeRequestIid, requestUrl, safeMessage(ex));
            return result("FAILED", safeMessage(ex), mergeRequestIid, requestUrl);
        }
    }

    private String resolveMergeRequestIid(GitLabMergeRequestNoteRequest request, Long reviewId) {
        if (request.getMergeRequestIid() != null && !request.getMergeRequestIid().isBlank()) {
            return request.getMergeRequestIid().trim();
        }
        return triggerRepository.findByReviewId(reviewId)
                .map(IntegrationWebhookReviewTrigger::getMergeRequestIid)
                .orElse(null);
    }

    private void safeRecord(String status, Long reviewId, String iid, String requestUrl, String errorMessage) {
        try {
            LocalDateTime now = LocalDateTime.now();
            IntegrationActionLog action = new IntegrationActionLog();
            action.setConnectorKey(CONNECTOR_KEY);
            action.setProvider("GITLAB");
            action.setActionType("MR_SUMMARY_NOTE");
            action.setActionStatus(status);
            action.setTargetKey(iid == null ? "review:" + reviewId : "mr:" + iid);
            action.setRequestUrl(requestUrl);
            action.setErrorMessage(errorMessage);
            action.setCreatedAt(now);
            action.setUpdatedAt(now);
            actionLogRepository.save(action);
        } catch (Exception ex) {
            log.warn("Failed to record GitLab MR summary action", ex);
        }
    }

    private GitLabMergeRequestNoteResultVO result(
            String status, String message, String mergeRequestIid, String requestUrl) {
        GitLabMergeRequestNoteResultVO result = new GitLabMergeRequestNoteResultVO();
        result.setStatus(status);
        result.setMessage(message);
        result.setMergeRequestIid(mergeRequestIid);
        result.setRequestUrl(requestUrl);
        return result;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
