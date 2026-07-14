package com.review.agent.service.impl;

import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.domain.dto.JenkinsReviewTriggerRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.service.JenkinsReviewTriggerService;
import com.review.agent.service.PrePrGateService;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsReviewTriggerServiceImpl implements JenkinsReviewTriggerService {

    private static final String DEFAULT_CONNECTOR_KEY = "jenkins-pipeline";
    private static final String PROVIDER = "JENKINS";
    private static final String EVENT_TYPE = "PIPELINE_REVIEW";

    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final IntegrationWebhookReviewTriggerRepository triggerRepository;
    private final ReviewService reviewService;
    private final PrePrGateService prePrGateService;

    @Override
    public IntegrationWebhookDeliveryResultVO receive(
            String connectorKey,
            String deliveryId,
            String token,
            JenkinsReviewTriggerRequest request) {
        String key = normalizeConnectorKey(connectorKey);
        CiStatusConfig config = requireConfig(key);
        String effectiveDeliveryId = effectiveDeliveryId(deliveryId, request);
        if (!secureEquals(config.getWebhookSecret(), token)) {
            saveDelivery(key, effectiveDeliveryId, request, "REJECTED", "Invalid Jenkins review token", null, null, null, null, null);
            throw new IllegalArgumentException("Invalid Jenkins review token");
        }
        requireProjectScope(config, request.getProjectId());

        IntegrationWebhookDeliveryLog existing = deliveryRepository
                .findByDeliveryId(key, effectiveDeliveryId)
                .orElse(null);
        if (existing != null) {
            return duplicate(existing);
        }

        if (Boolean.FALSE.equals(config.getChecksEnabled())) {
            saveDelivery(key, effectiveDeliveryId, request, "ACCEPTED", null, "SKIPPED", null, null,
                    "Jenkins connector is disabled", 0);
            return result(effectiveDeliveryId, false, "SKIPPED", null, null,
                    "Jenkins connector is disabled", 0, null);
        }

        String externalEventId = request.getJobName().trim() + "#" + request.getBuildNumber().trim();
        String triggerKey = triggerKey(request, externalEventId);
        boolean reserved = triggerRepository.tryReserve(
                key,
                triggerKey,
                request.getProjectId(),
                request.getSourceBranch().trim(),
                request.getTargetBranch().trim(),
                externalEventId,
                trimToNull(request.getBuildUrl()),
                trimToNull(request.getCommitSha()));
        if (!reserved) {
            IntegrationWebhookReviewTrigger trigger = triggerRepository.find(key, triggerKey).orElse(null);
            saveDelivery(
                    key,
                    effectiveDeliveryId,
                    request,
                    "DUPLICATE",
                    null,
                    trigger == null ? "UNKNOWN" : trigger.getTriggerStatus(),
                    triggerKey,
                    trigger == null ? null : trigger.getReviewId(),
                    trigger == null ? "Review trigger already exists" : trigger.getMessage(),
                    trigger == null ? null : trigger.getRetryCount());
            return result(
                    effectiveDeliveryId,
                    true,
                    trigger == null ? "UNKNOWN" : trigger.getTriggerStatus(),
                    triggerKey,
                    trigger == null ? null : trigger.getReviewId(),
                    trigger == null ? "Review trigger already exists" : trigger.getMessage(),
                    trigger == null ? null : trigger.getRetryCount(),
                    trigger == null ? null : trigger.getNextRetryAt());
        }

        return processReserved(key, effectiveDeliveryId, triggerKey, request);
    }

    @Override
    public PrePrGateVO getGate(String connectorKey, String token, Long reviewId) {
        String key = normalizeConnectorKey(connectorKey);
        CiStatusConfig config = requireConfig(key);
        if (!secureEquals(config.getWebhookSecret(), token)) {
            throw new IllegalArgumentException("Invalid Jenkins review token");
        }
        triggerRepository.findByReviewId(key, reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Review was not triggered by Jenkins pipeline: " + reviewId));
        return prePrGateService.getGate(reviewId);
    }

    private IntegrationWebhookDeliveryResultVO processReserved(
            String connectorKey,
            String deliveryId,
            String triggerKey,
            JenkinsReviewTriggerRequest request) {
        try {
            CreatePrePrRequest createRequest = new CreatePrePrRequest();
            createRequest.setProjectId(request.getProjectId());
            createRequest.setSourceBranch(request.getSourceBranch().trim());
            createRequest.setTargetBranch(request.getTargetBranch().trim());
            ReviewDetailVO detail = reviewService.createPrePrReview(createRequest);
            Long reviewId = detail == null || detail.getReview() == null ? null : detail.getReview().getId();
            if (reviewId == null) {
                throw new IllegalStateException("Pre-PR review was created without a review id");
            }
            triggerRepository.markProcessed(connectorKey, triggerKey, reviewId);
            saveDelivery(connectorKey, deliveryId, request, "ACCEPTED", null, "PROCESSED", triggerKey, reviewId, null, 0);
            return result(deliveryId, false, "PROCESSED", triggerKey, reviewId, null, 0, null);
        } catch (Exception ex) {
            String message = safeMessage(ex);
            LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(2);
            triggerRepository.markFailed(connectorKey, triggerKey, message, nextRetryAt);
            saveDelivery(connectorKey, deliveryId, request, "ACCEPTED", null, "FAILED", triggerKey, null, message, 1, nextRetryAt);
            log.warn("Failed to trigger Pre-PR review from Jenkins build {}", triggerKey, ex);
            return result(deliveryId, false, "FAILED", triggerKey, null, message, 1, nextRetryAt);
        }
    }

    private IntegrationWebhookDeliveryResultVO duplicate(IntegrationWebhookDeliveryLog existing) {
        return result(
                existing.getDeliveryId(),
                true,
                existing.getTriggerStatus(),
                existing.getTriggerKey(),
                existing.getTriggerReviewId(),
                existing.getTriggerMessage(),
                existing.getTriggerRetryCount(),
                existing.getTriggerNextRetryAt());
    }

    private IntegrationWebhookDeliveryResultVO result(
            String deliveryId,
            boolean duplicate,
            String triggerStatus,
            String triggerKey,
            Long reviewId,
            String message,
            Integer retryCount,
            LocalDateTime nextRetryAt) {
        IntegrationWebhookDeliveryResultVO result = new IntegrationWebhookDeliveryResultVO();
        result.setDeliveryId(deliveryId);
        result.setEventType(EVENT_TYPE);
        result.setStatus(duplicate ? "DUPLICATE" : "ACCEPTED");
        result.setDuplicate(duplicate);
        result.setTriggerStatus(triggerStatus);
        result.setTriggerKey(triggerKey);
        result.setTriggerReviewId(reviewId);
        result.setTriggerMessage(message);
        result.setTriggerRetryCount(retryCount);
        result.setTriggerNextRetryAt(nextRetryAt);
        return result;
    }

    private void saveDelivery(
            String connectorKey,
            String deliveryId,
            JenkinsReviewTriggerRequest request,
            String deliveryStatus,
            String errorMessage,
            String triggerStatus,
            String triggerKey,
            Long reviewId,
            String triggerMessage,
            Integer retryCount) {
        saveDelivery(connectorKey, deliveryId, request, deliveryStatus, errorMessage, triggerStatus, triggerKey, reviewId,
                triggerMessage, retryCount, null);
    }

    private void saveDelivery(
            String connectorKey,
            String deliveryId,
            JenkinsReviewTriggerRequest request,
            String deliveryStatus,
            String errorMessage,
            String triggerStatus,
            String triggerKey,
            Long reviewId,
            String triggerMessage,
            Integer retryCount,
            LocalDateTime nextRetryAt) {
        LocalDateTime now = LocalDateTime.now();
        IntegrationWebhookDeliveryLog logEntry = new IntegrationWebhookDeliveryLog();
        logEntry.setConnectorKey(connectorKey);
        logEntry.setProvider(PROVIDER);
        logEntry.setDeliveryId(deliveryId);
        logEntry.setEventType(EVENT_TYPE);
        logEntry.setDeliveryStatus(deliveryStatus);
        logEntry.setPayloadDigest(sha256(stableIdentity(request)));
        logEntry.setErrorMessage(errorMessage);
        logEntry.setTriggerStatus(triggerStatus);
        logEntry.setTriggerKey(triggerKey);
        logEntry.setTriggerReviewId(reviewId);
        logEntry.setTriggerMessage(triggerMessage);
        logEntry.setTriggerRetryCount(retryCount);
        logEntry.setTriggerNextRetryAt(nextRetryAt);
        logEntry.setReceivedAt(now);
        logEntry.setProcessedAt(now);
        logEntry.setCreatedAt(now);
        logEntry.setUpdatedAt(now);
        deliveryRepository.save(logEntry);
    }

    private String effectiveDeliveryId(String deliveryId, JenkinsReviewTriggerRequest request) {
        if (deliveryId != null && !deliveryId.isBlank()) {
            String normalized = deliveryId.trim();
            return normalized.length() <= 200 ? normalized : "jenkins:" + sha256(normalized);
        }
        return "jenkins:" + sha256(stableIdentity(request));
    }

    private String triggerKey(JenkinsReviewTriggerRequest request, String externalEventId) {
        String identity = request.getProjectId() + "|" + externalEventId + "|"
                + nullToEmpty(request.getCommitSha()) + "|" + request.getSourceBranch() + "|" + request.getTargetBranch();
        return "project:" + request.getProjectId() + ":jenkins:" + sha256(identity);
    }

    private String stableIdentity(JenkinsReviewTriggerRequest request) {
        return request.getProjectId() + "|" + request.getJobName() + "|" + request.getBuildNumber() + "|"
                + nullToEmpty(request.getBuildUrl()) + "|" + nullToEmpty(request.getCommitSha()) + "|"
                + request.getSourceBranch() + "|" + request.getTargetBranch();
    }

    private boolean secureEquals(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate Jenkins delivery digest", ex);
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private CiStatusConfig requireConfig(String connectorKey) {
        CiStatusConfig config = deliveryRepository.findConfig(connectorKey)
                .orElseThrow(() -> new IllegalStateException("Jenkins connector config not found: " + connectorKey));
        if (!PROVIDER.equalsIgnoreCase(config.getProvider())) {
            throw new IllegalArgumentException("Connector is not a Jenkins instance: " + connectorKey);
        }
        return config;
    }

    private void requireProjectScope(CiStatusConfig config, Long projectId) {
        if (config.getProjectId() != null && !config.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "Jenkins connector is not authorized for project " + projectId);
        }
    }

    private String normalizeConnectorKey(String connectorKey) {
        String key = connectorKey == null || connectorKey.isBlank()
                ? DEFAULT_CONNECTOR_KEY
                : connectorKey.trim().toLowerCase();
        if (!key.equals(DEFAULT_CONNECTOR_KEY) && !key.startsWith(DEFAULT_CONNECTOR_KEY + ":")) {
            throw new IllegalArgumentException("Invalid Jenkins connector key: " + key);
        }
        return key;
    }
}
