package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface IntegrationWebhookDeliveryRepository {

    Optional<CiStatusConfig> findConfig(String connectorKey);

    Optional<IntegrationWebhookDeliveryLog> findByDeliveryId(String connectorKey, String deliveryId);

    List<IntegrationWebhookDeliveryLog> listRecent(int limit);

    void save(IntegrationWebhookDeliveryLog log);

    void updateTriggerResult(
            String triggerKey,
            String status,
            Long reviewId,
            String message,
            Integer retryCount,
            LocalDateTime nextRetryAt);

    default void updateTriggerResult(
            String connectorKey,
            String triggerKey,
            String status,
            Long reviewId,
            String message,
            Integer retryCount,
            LocalDateTime nextRetryAt) {
        updateTriggerResult(triggerKey, status, reviewId, message, retryCount, nextRetryAt);
    }
}
