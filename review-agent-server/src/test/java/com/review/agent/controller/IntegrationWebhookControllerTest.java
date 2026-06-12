package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationWebhookControllerTest {

    private final RecordingDeliveryService deliveryService = new RecordingDeliveryService();
    private final IntegrationWebhookController controller = new IntegrationWebhookController(deliveryService);

    @Test
    void receiveGitHubDelegatesHeadersAndPayloadToDeliveryService() {
        Result<IntegrationWebhookDeliveryResultVO> result = controller.receiveGitHub(
                "delivery-1",
                "pull_request",
                "sha256=abc",
                "{\"action\":\"opened\"}");

        assertTrue(result.isSuccess());
        assertEquals("delivery-1", result.getData().getDeliveryId());
        assertEquals("ACCEPTED", result.getData().getStatus());
        assertEquals("delivery-1", deliveryService.deliveryId);
        assertEquals("pull_request", deliveryService.eventType);
        assertEquals("sha256=abc", deliveryService.signature);
        assertEquals("{\"action\":\"opened\"}", deliveryService.payload);
    }

    private static class RecordingDeliveryService implements IntegrationWebhookDeliveryService {
        private String deliveryId;
        private String eventType;
        private String signature;
        private String payload;

        @Override
        public IntegrationWebhookDeliveryResultVO receiveGitHubDelivery(
                String deliveryId,
                String eventType,
                String signature,
                String payload) {
            this.deliveryId = deliveryId;
            this.eventType = eventType;
            this.signature = signature;
            this.payload = payload;
            IntegrationWebhookDeliveryResultVO result = new IntegrationWebhookDeliveryResultVO();
            result.setDeliveryId(deliveryId);
            result.setEventType(eventType);
            result.setStatus("ACCEPTED");
            result.setDuplicate(false);
            return result;
        }
    }
}
