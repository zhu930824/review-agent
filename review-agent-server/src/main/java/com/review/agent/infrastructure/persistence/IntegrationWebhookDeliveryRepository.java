package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;

import java.util.Optional;

public interface IntegrationWebhookDeliveryRepository {

    Optional<CiStatusConfig> findConfig(String connectorKey);

    Optional<IntegrationWebhookDeliveryLog> findByDeliveryId(String connectorKey, String deliveryId);

    void save(IntegrationWebhookDeliveryLog log);
}
