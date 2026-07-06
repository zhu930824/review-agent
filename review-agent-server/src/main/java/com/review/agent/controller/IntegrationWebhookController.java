package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.service.IntegrationWebhookDeliveryLogService;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/webhooks")
@RequiredArgsConstructor
public class IntegrationWebhookController {

    private final IntegrationWebhookDeliveryService deliveryService;
    private final IntegrationWebhookDeliveryLogService deliveryLogService;

    @GetMapping("/deliveries")
    public Result<List<IntegrationWebhookDeliveryLogVO>> listDeliveries(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.success(deliveryLogService.listRecent(limit));
    }

    @PostMapping("/github")
    public Result<IntegrationWebhookDeliveryResultVO> receiveGitHub(
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {
        return Result.success(deliveryService.receiveGitHubDelivery(deliveryId, eventType, signature, payload));
    }

    @PostMapping("/gitlab")
    public Result<IntegrationWebhookDeliveryResultVO> receiveGitLab(
            @RequestHeader(value = "X-Gitlab-Delivery", required = false) String deliveryId,
            @RequestHeader(value = "X-Gitlab-Event", defaultValue = "unknown") String eventType,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestHeader(value = "X-GitLab-Token", required = false) String legacyToken,
            @RequestBody String payload) {
        String effectiveToken = token == null ? legacyToken : token;
        return Result.success(deliveryService.receiveGitLabDelivery(deliveryId, eventType, effectiveToken, payload));
    }
}
