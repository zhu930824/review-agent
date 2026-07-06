package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.infrastructure.webhook.GitHubWebhookSignatureVerifier;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class IntegrationWebhookDeliveryServiceImpl implements IntegrationWebhookDeliveryService {

    private static final String GITHUB_CONNECTOR_KEY = "github-checks";
    private static final String GITHUB_PROVIDER = "GITHUB";
    private static final String GITLAB_CONNECTOR_KEY = "gitlab-merge-request";
    private static final String GITLAB_PROVIDER = "GITLAB";

    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final GitHubWebhookSignatureVerifier signatureVerifier;

    @Override
    public IntegrationWebhookDeliveryResultVO receiveGitHubDelivery(
            String deliveryId,
            String eventType,
            String signature,
            String payload) {
        if (!hasText(deliveryId)) {
            throw new IllegalArgumentException("GitHub delivery id is required");
        }
        if (deliveryRepository.findByDeliveryId(GITHUB_CONNECTOR_KEY, deliveryId).isPresent()) {
            return result(deliveryId, eventType, "DUPLICATE", true);
        }

        CiStatusConfig config = deliveryRepository.findConfig(GITHUB_CONNECTOR_KEY)
                .orElseThrow(() -> new IllegalStateException("GitHub webhook config not found"));
        if (!signatureVerifier.isValid(config.getWebhookSecret(), payload, signature)) {
            saveLog(GITHUB_CONNECTOR_KEY, GITHUB_PROVIDER, deliveryId, eventType, signature, payload, "REJECTED", "Invalid GitHub webhook signature");
            throw new IllegalArgumentException("Invalid GitHub webhook signature");
        }

        saveLog(GITHUB_CONNECTOR_KEY, GITHUB_PROVIDER, deliveryId, eventType, signature, payload, "ACCEPTED", null);
        return result(deliveryId, eventType, "ACCEPTED", false);
    }

    @Override
    public IntegrationWebhookDeliveryResultVO receiveGitLabDelivery(
            String deliveryId,
            String eventType,
            String token,
            String payload) {
        String effectiveDeliveryId = hasText(deliveryId)
                ? deliveryId
                : "gitlab:" + nullToUnknown(eventType) + ":" + sha256(payload == null ? "" : payload);
        if (deliveryRepository.findByDeliveryId(GITLAB_CONNECTOR_KEY, effectiveDeliveryId).isPresent()) {
            return result(effectiveDeliveryId, eventType, "DUPLICATE", true);
        }

        CiStatusConfig config = deliveryRepository.findConfig(GITLAB_CONNECTOR_KEY)
                .orElseThrow(() -> new IllegalStateException("GitLab webhook config not found"));
        if (!hasText(config.getWebhookSecret()) || !config.getWebhookSecret().equals(token)) {
            saveLog(GITLAB_CONNECTOR_KEY, GITLAB_PROVIDER, effectiveDeliveryId, eventType, token, payload, "REJECTED", "Invalid GitLab webhook token");
            throw new IllegalArgumentException("Invalid GitLab webhook token");
        }

        saveLog(GITLAB_CONNECTOR_KEY, GITLAB_PROVIDER, effectiveDeliveryId, eventType, token, payload, "ACCEPTED", null);
        return result(effectiveDeliveryId, eventType, "ACCEPTED", false);
    }

    private void saveLog(
            String connectorKey,
            String provider,
            String deliveryId,
            String eventType,
            String signature,
            String payload,
            String status,
            String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        IntegrationWebhookDeliveryLog log = new IntegrationWebhookDeliveryLog();
        log.setConnectorKey(connectorKey);
        log.setProvider(provider);
        log.setDeliveryId(deliveryId);
        log.setEventType(eventType);
        log.setSignature(signature);
        log.setPayloadDigest(sha256(payload == null ? "" : payload));
        log.setDeliveryStatus(status);
        log.setErrorMessage(errorMessage);
        log.setReceivedAt(now);
        log.setProcessedAt(now);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        deliveryRepository.save(log);
    }

    private IntegrationWebhookDeliveryResultVO result(String deliveryId, String eventType, String status, boolean duplicate) {
        IntegrationWebhookDeliveryResultVO result = new IntegrationWebhookDeliveryResultVO();
        result.setDeliveryId(deliveryId);
        result.setEventType(eventType);
        result.setStatus(status);
        result.setDuplicate(duplicate);
        return result;
    }

    private String nullToUnknown(String value) {
        return hasText(value) ? value : "unknown";
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate webhook payload digest", ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
