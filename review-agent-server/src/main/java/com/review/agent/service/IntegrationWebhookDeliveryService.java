package com.review.agent.service;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;

public interface IntegrationWebhookDeliveryService {

    IntegrationWebhookDeliveryResultVO receiveGitHubDelivery(
            String deliveryId,
            String eventType,
            String signature,
            String payload);

    IntegrationWebhookDeliveryResultVO receiveGitLabDelivery(
            String deliveryId,
            String eventType,
            String token,
            String payload);
}
