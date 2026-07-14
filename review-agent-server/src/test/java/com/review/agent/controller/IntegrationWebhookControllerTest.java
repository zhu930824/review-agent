package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.service.IntegrationWebhookDeliveryLogService;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import com.review.agent.service.GitLabReviewTriggerRetryService;
import com.review.agent.service.GitLabReviewTriggerHealthService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class IntegrationWebhookControllerTest {

    private final RecordingDeliveryService deliveryService = new RecordingDeliveryService();
    private final RecordingDeliveryLogService deliveryLogService = new RecordingDeliveryLogService();
    private final GitLabReviewTriggerRetryService retryService = mock(GitLabReviewTriggerRetryService.class);
    private final GitLabReviewTriggerHealthService healthService = mock(GitLabReviewTriggerHealthService.class);
    private final IntegrationWebhookController controller = new IntegrationWebhookController(
            deliveryService, deliveryLogService, retryService, healthService);

    @Test
    void listDeliveriesDelegatesToLogService() {
        Result<List<IntegrationWebhookDeliveryLogVO>> result = controller.listDeliveries(12);

        assertTrue(result.isSuccess());
        assertEquals(12, deliveryLogService.limit);
        assertEquals("gitlab-merge-request", result.getData().get(0).getConnectorKey());
        assertEquals("ACCEPTED", result.getData().get(0).getDeliveryStatus());
    }

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

    @Test
    void receiveGitLabDelegatesHeadersAndPayloadToDeliveryService() {
        Result<IntegrationWebhookDeliveryResultVO> result = controller.receiveGitLab(
                "gitlab-delivery-1",
                "Merge Request Hook",
                "token-1",
                null,
                "{\"object_kind\":\"merge_request\"}");

        assertTrue(result.isSuccess());
        assertEquals("gitlab-delivery-1", result.getData().getDeliveryId());
        assertEquals("ACCEPTED", result.getData().getStatus());
        assertEquals("gitlab-delivery-1", deliveryService.gitLabDeliveryId);
        assertEquals("Merge Request Hook", deliveryService.gitLabEventType);
        assertEquals("token-1", deliveryService.gitLabToken);
        assertEquals("{\"object_kind\":\"merge_request\"}", deliveryService.gitLabPayload);
    }

    private static class RecordingDeliveryService implements IntegrationWebhookDeliveryService {
        private String deliveryId;
        private String eventType;
        private String signature;
        private String payload;
        private String gitLabDeliveryId;
        private String gitLabEventType;
        private String gitLabToken;
        private String gitLabPayload;

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

        @Override
        public IntegrationWebhookDeliveryResultVO receiveGitLabDelivery(
                String deliveryId,
                String eventType,
                String token,
                String payload) {
            gitLabDeliveryId = deliveryId;
            gitLabEventType = eventType;
            gitLabToken = token;
            gitLabPayload = payload;
            IntegrationWebhookDeliveryResultVO result = new IntegrationWebhookDeliveryResultVO();
            result.setDeliveryId(deliveryId);
            result.setEventType(eventType);
            result.setStatus("ACCEPTED");
            result.setDuplicate(false);
            return result;
        }
    }

    private static class RecordingDeliveryLogService implements IntegrationWebhookDeliveryLogService {
        private int limit;

        @Override
        public List<IntegrationWebhookDeliveryLogVO> listRecent(int limit) {
            this.limit = limit;
            IntegrationWebhookDeliveryLogVO vo = new IntegrationWebhookDeliveryLogVO();
            vo.setConnectorKey("gitlab-merge-request");
            vo.setDeliveryStatus("ACCEPTED");
            return List.of(vo);
        }
    }
}
