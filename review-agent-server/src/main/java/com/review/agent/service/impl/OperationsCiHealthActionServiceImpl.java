package com.review.agent.service.impl;

import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.notification.OperationsCiHealthNotificationClient;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.service.CiStatusConfigService;
import com.review.agent.service.CiStatusWritebackLogService;
import com.review.agent.service.OperationsCiHealthActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsCiHealthActionServiceImpl implements OperationsCiHealthActionService {

    private static final int HEALTH_SAMPLE_LIMIT = 50;
    private static final String NOTIFICATION_ACTION_TYPE = "CI_HEALTH_NOTIFICATION";

    private final CiStatusWritebackLogService ciStatusWritebackLogService;
    private final CiStatusConfigService ciStatusConfigService;
    private final IntegrationActionLogRepository integrationActionLogRepository;
    private final OperationsCiHealthNotificationClient notificationClient;

    @Override
    public List<OperationsCiHealthActionVO> listActions() {
        return ciStatusWritebackLogService.listHealth(HEALTH_SAMPLE_LIMIT).stream()
                .filter(health -> !"HEALTHY".equals(health.getHealthStatus()))
                .map(this::toAction)
                .toList();
    }

    @Override
    public IntegrationActionLogVO notifyAction(String actionKey) {
        OperationsCiHealthActionVO action = listActions().stream()
                .filter(item -> item.getKey().equals(actionKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CI health action not found: " + actionKey));
        CiStatusConfigVO config = ciStatusConfigService.getConfig(action.getConnectorKey());
        String webhookUrl = config == null ? null : config.getNotificationWebhookUrl();
        if (!hasText(webhookUrl)) {
            return recordNotification(action, "SKIPPED", null, "Notification webhook is not configured");
        }
        try {
            notificationClient.send(webhookUrl, action);
            return recordNotification(action, "POSTED", webhookUrl, null);
        } catch (Exception ex) {
            return recordNotification(action, "FAILED", webhookUrl, ex.getMessage());
        }
    }

    private OperationsCiHealthActionVO toAction(CiIntegrationHealthVO health) {
        OperationsCiHealthActionVO vo = new OperationsCiHealthActionVO();
        vo.setKey(health.getConnectorKey() + "-" + health.getHealthStatus());
        vo.setConnectorKey(health.getConnectorKey());
        vo.setProvider(health.getProvider());
        vo.setHealthStatus(health.getHealthStatus());
        vo.setSeverity(severity(health.getHealthStatus()));
        vo.setOwnerRole(ownerRole(health.getProvider()));
        vo.setSlaHours(slaHours(health.getHealthStatus()));
        vo.setLatestWritebackId(health.getLatestWritebackId());
        vo.setLatestWritebackStatus(health.getLatestWritebackStatus());
        vo.setLatestSignal(latestSignal(health));
        vo.setLatestRequestUrl(health.getLatestRequestUrl());
        vo.setLatestExternalQueueUrl(health.getLatestExternalQueueUrl());
        vo.setLatestExternalBuildUrl(health.getLatestExternalBuildUrl());
        vo.setRecommendation(recommendation(health));
        vo.setNotificationPriority(notificationPriority(vo.getSeverity()));
        vo.setNotificationDedupKey("ci-health:" + vo.getConnectorKey() + ":" + vo.getHealthStatus());
        vo.setNotificationTitle(notificationTitle(vo));
        vo.setNotificationBody(notificationBody(vo));
        vo.setNotificationTargetUrl(notificationTargetUrl(health));
        return vo;
    }

    private String severity(String healthStatus) {
        if ("UNHEALTHY".equals(healthStatus)) {
            return "CRITICAL";
        }
        if ("DEGRADED".equals(healthStatus)) {
            return "WARNING";
        }
        return "INFO";
    }

    private Long slaHours(String healthStatus) {
        if ("UNHEALTHY".equals(healthStatus)) {
            return 4L;
        }
        if ("DEGRADED".equals(healthStatus)) {
            return 24L;
        }
        return 72L;
    }

    private String ownerRole(String provider) {
        if ("JENKINS".equalsIgnoreCase(provider)) {
            return "CI Owner";
        }
        if ("GITLAB".equalsIgnoreCase(provider)) {
            return "GitLab Owner";
        }
        if ("GITHUB".equalsIgnoreCase(provider)) {
            return "GitHub Owner";
        }
        return "Integration Owner";
    }

    private String latestSignal(CiIntegrationHealthVO health) {
        String writeback = health.getLatestWritebackStatus() == null ? "no writeback" : health.getLatestWritebackStatus();
        if (health.getLatestExternalResult() == null || health.getLatestExternalResult().isBlank()) {
            return writeback;
        }
        return writeback + " / " + health.getLatestExternalResult();
    }

    private String recommendation(CiIntegrationHealthVO health) {
        if ("UNHEALTHY".equals(health.getHealthStatus())) {
            return "Check credentials, repository binding, permissions, and the latest failed writeback before the next release gate.";
        }
        if ("DEGRADED".equals(health.getHealthStatus())) {
            return "Review retry history and external build state; refresh Jenkins results when this action belongs to a pipeline connector.";
        }
        return "Run a gate publish smoke test so the workbench has fresh CI evidence for this connector.";
    }

    private String notificationPriority(String severity) {
        if ("CRITICAL".equals(severity)) {
            return "P1";
        }
        if ("WARNING".equals(severity)) {
            return "P2";
        }
        return "P3";
    }

    private String notificationTitle(OperationsCiHealthActionVO action) {
        return "[" + action.getNotificationPriority() + "] " + action.getProvider()
                + " CI health " + action.getHealthStatus()
                + " for " + action.getConnectorKey();
    }

    private String notificationBody(OperationsCiHealthActionVO action) {
        return action.getLatestSignal()
                + " | owner=" + action.getOwnerRole()
                + " | sla=" + action.getSlaHours() + "h"
                + " | next=" + action.getRecommendation();
    }

    private String notificationTargetUrl(CiIntegrationHealthVO health) {
        if (health.getLatestExternalBuildUrl() != null && !health.getLatestExternalBuildUrl().isBlank()) {
            return health.getLatestExternalBuildUrl();
        }
        if (health.getLatestExternalQueueUrl() != null && !health.getLatestExternalQueueUrl().isBlank()) {
            return health.getLatestExternalQueueUrl();
        }
        return health.getLatestRequestUrl();
    }

    private IntegrationActionLogVO recordNotification(
            OperationsCiHealthActionVO action,
            String status,
            String requestUrl,
            String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        IntegrationActionLog log = new IntegrationActionLog();
        log.setConnectorKey(action.getConnectorKey());
        log.setProvider(action.getProvider());
        log.setActionType(NOTIFICATION_ACTION_TYPE);
        log.setActionStatus(status);
        log.setTargetKey(action.getNotificationDedupKey());
        log.setRequestUrl(requestUrl);
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        integrationActionLogRepository.save(log);
        return toActionLogVO(log);
    }

    private IntegrationActionLogVO toActionLogVO(IntegrationActionLog log) {
        IntegrationActionLogVO vo = new IntegrationActionLogVO();
        vo.setId(log.getId());
        vo.setConnectorKey(log.getConnectorKey());
        vo.setProvider(log.getProvider());
        vo.setActionType(log.getActionType());
        vo.setActionStatus(log.getActionStatus());
        vo.setTargetKey(log.getTargetKey());
        vo.setCommitSha(log.getCommitSha());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());
        return vo;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
