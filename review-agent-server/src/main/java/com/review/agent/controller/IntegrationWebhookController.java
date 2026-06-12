package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/webhooks")
@RequiredArgsConstructor
public class IntegrationWebhookController {

    private final IntegrationWebhookDeliveryService deliveryService;

    @PostMapping("/github")
    public Result<IntegrationWebhookDeliveryResultVO> receiveGitHub(
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {
        return Result.success(deliveryService.receiveGitHubDelivery(deliveryId, eventType, signature, payload));
    }
}
