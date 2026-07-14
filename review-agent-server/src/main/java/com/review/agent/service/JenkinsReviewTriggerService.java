package com.review.agent.service;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.domain.dto.JenkinsReviewTriggerRequest;
import com.review.agent.domain.dto.PrePrGateVO;

public interface JenkinsReviewTriggerService {

    IntegrationWebhookDeliveryResultVO receive(
            String connectorKey,
            String deliveryId,
            String token,
            JenkinsReviewTriggerRequest request);

    PrePrGateVO getGate(String connectorKey, String token, Long reviewId);
}
