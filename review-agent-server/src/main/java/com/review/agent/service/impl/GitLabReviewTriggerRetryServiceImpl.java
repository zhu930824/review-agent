package com.review.agent.service.impl;

import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.domain.dto.GitLabMergeRequestNoteResultVO;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.infrastructure.webhook.GitLabMergeRequestReviewTriggerResult;
import com.review.agent.service.GitLabReviewTriggerRetryService;
import com.review.agent.service.ReviewService;
import com.review.agent.service.GitLabMergeRequestNoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabReviewTriggerRetryServiceImpl implements GitLabReviewTriggerRetryService {

    private static final String CONNECTOR_KEY = "gitlab-merge-request";
    private static final int DUE_RETRY_BATCH_SIZE = 20;
    private static final int MAX_RETRY_COUNT = 5;

    private final IntegrationWebhookReviewTriggerRepository triggerRepository;
    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final ReviewService reviewService;
    private final ProjectGitLabConfigMapper gitLabConfigMapper;
    private final GitLabMergeRequestNoteService mergeRequestNoteService;

    @Override
    public GitLabMergeRequestReviewTriggerResult retry(String triggerKey) {
        IntegrationWebhookReviewTrigger trigger = requireRetryableTrigger(triggerKey);
        if (!triggerRepository.claimRetryable(CONNECTOR_KEY, triggerKey)) {
            throw new IllegalStateException("GitLab review trigger is already being retried: " + triggerKey);
        }
        return process(trigger);
    }

    @Override
    public int retryDueTriggers() {
        List<IntegrationWebhookReviewTrigger> dueTriggers = triggerRepository.findDueFailed(
                CONNECTOR_KEY, LocalDateTime.now(), DUE_RETRY_BATCH_SIZE);
        int attempted = 0;
        for (IntegrationWebhookReviewTrigger trigger : dueTriggers) {
            if (!triggerRepository.claimFailed(CONNECTOR_KEY, trigger.getTriggerKey())) {
                continue;
            }
            process(trigger);
            attempted++;
        }
        return attempted;
    }

    private GitLabMergeRequestReviewTriggerResult process(IntegrationWebhookReviewTrigger trigger) {
        try {
            CreatePrePrRequest request = new CreatePrePrRequest();
            request.setProjectId(trigger.getProjectId());
            request.setSourceBranch(trigger.getSourceBranch());
            request.setTargetBranch(trigger.getTargetBranch());
            ReviewDetailVO detail = reviewService.createPrePrReview(request);
            Long reviewId = detail == null || detail.getReview() == null ? null : detail.getReview().getId();
            if (reviewId == null) {
                throw new IllegalStateException("Pre-PR review was created without a review id");
            }
            triggerRepository.markProcessed(CONNECTOR_KEY, trigger.getTriggerKey(), reviewId);
            String completionMessage = autoPublishSummary(trigger, reviewId);
            deliveryRepository.updateTriggerResult(
                    CONNECTOR_KEY, trigger.getTriggerKey(), "PROCESSED", reviewId, completionMessage, trigger.getRetryCount(), null);
            return GitLabMergeRequestReviewTriggerResult.processed(
                    trigger.getTriggerKey(), reviewId, completionMessage);
        } catch (Exception ex) {
            String message = safeMessage(ex);
            int retryCount = trigger.getRetryCount() == null ? 1 : trigger.getRetryCount() + 1;
            if (retryCount >= MAX_RETRY_COUNT) {
                triggerRepository.markExhausted(CONNECTOR_KEY, trigger.getTriggerKey(), message);
                deliveryRepository.updateTriggerResult(
                        CONNECTOR_KEY, trigger.getTriggerKey(), "EXHAUSTED", null, message, retryCount, null);
                log.warn("GitLab review trigger {} exhausted after {} attempts: {}",
                        trigger.getTriggerKey(), retryCount, message);
                return GitLabMergeRequestReviewTriggerResult.exhausted(
                        trigger.getTriggerKey(), message, retryCount);
            }
            LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(nextRetryDelayMinutes(trigger.getRetryCount()));
            triggerRepository.markFailed(CONNECTOR_KEY, trigger.getTriggerKey(), message, nextRetryAt);
            deliveryRepository.updateTriggerResult(
                    CONNECTOR_KEY, trigger.getTriggerKey(), "FAILED", null, message, retryCount, nextRetryAt);
            log.warn("Failed to retry GitLab review trigger {}: {}", trigger.getTriggerKey(), message);
            return GitLabMergeRequestReviewTriggerResult.failed(
                    trigger.getTriggerKey(), message, retryCount, nextRetryAt);
        }
    }

    private IntegrationWebhookReviewTrigger requireRetryableTrigger(String triggerKey) {
        IntegrationWebhookReviewTrigger trigger = triggerRepository.find(CONNECTOR_KEY, triggerKey)
                .orElseThrow(() -> new IllegalArgumentException("GitLab review trigger not found: " + triggerKey));
        if (!List.of("FAILED", "EXHAUSTED").contains(trigger.getTriggerStatus())) {
            throw new IllegalStateException("Only failed or exhausted GitLab review triggers can be retried");
        }
        return trigger;
    }

    private String autoPublishSummary(IntegrationWebhookReviewTrigger trigger, Long reviewId) {
        ProjectGitLabConfig config = gitLabConfigMapper.selectOne(new LambdaQueryWrapper<ProjectGitLabConfig>()
                .eq(ProjectGitLabConfig::getProjectId, trigger.getProjectId())
                .eq(ProjectGitLabConfig::getEnabled, true));
        if (config == null || !Boolean.TRUE.equals(config.getPublishSummaryEnabled())) {
            return null;
        }
        try {
            GitLabMergeRequestNoteResultVO noteResult = mergeRequestNoteService.postAutoSummary(
                    reviewId, trigger.getMergeRequestIid());
            return "GitLab MR summary: " + noteResult.getStatus()
                    + (noteResult.getMessage() == null ? "" : " - " + noteResult.getMessage());
        } catch (Exception ex) {
            log.warn("Review {} retry completed but GitLab MR summary failed", reviewId, ex);
            return "Review completed; GitLab MR summary failed: " + safeMessage(ex);
        }
    }

    private long nextRetryDelayMinutes(Integer retryCount) {
        int attempts = retryCount == null ? 1 : Math.max(1, retryCount + 1);
        return Math.min(30L, 1L << Math.min(attempts, 5));
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
