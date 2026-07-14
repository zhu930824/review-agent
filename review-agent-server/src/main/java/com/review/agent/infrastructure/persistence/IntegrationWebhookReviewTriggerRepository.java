package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface IntegrationWebhookReviewTriggerRepository {

    boolean tryReserve(
            String connectorKey,
            String triggerKey,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String mergeRequestIid,
            String mergeRequestUrl);

    boolean tryReserve(
            String connectorKey,
            String triggerKey,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String externalEventId,
            String externalEventUrl,
            String commitSha);

    Optional<IntegrationWebhookReviewTrigger> find(String connectorKey, String triggerKey);

    Optional<IntegrationWebhookReviewTrigger> findByReviewId(Long reviewId);

    Optional<IntegrationWebhookReviewTrigger> findByReviewId(String connectorKey, Long reviewId);

    List<IntegrationWebhookReviewTrigger> findDueFailed(String connectorKey, LocalDateTime now, int limit);

    boolean claimFailed(String connectorKey, String triggerKey);

    boolean claimRetryable(String connectorKey, String triggerKey);

    void markProcessed(String connectorKey, String triggerKey, Long reviewId);

    void markFailed(String connectorKey, String triggerKey, String message, LocalDateTime nextRetryAt);

    void markExhausted(String connectorKey, String triggerKey, String message);

    long countByStatus(String connectorKey, String status);

    Optional<IntegrationWebhookReviewTrigger> findOldestPending(String connectorKey);
}
