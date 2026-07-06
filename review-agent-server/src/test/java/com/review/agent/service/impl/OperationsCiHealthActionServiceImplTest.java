package com.review.agent.service.impl;

import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.notification.OperationsCiHealthNotificationClient;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.service.CiStatusConfigService;
import com.review.agent.service.CiStatusWritebackLogService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsCiHealthActionServiceImplTest {

    private final FakeCiStatusWritebackLogService writebackLogService = new FakeCiStatusWritebackLogService();
    private final FakeCiStatusConfigService configService = new FakeCiStatusConfigService();
    private final FakeIntegrationActionLogRepository actionLogRepository = new FakeIntegrationActionLogRepository();
    private final FakeNotificationClient notificationClient = new FakeNotificationClient();
    private final OperationsCiHealthActionServiceImpl service = new OperationsCiHealthActionServiceImpl(
            writebackLogService,
            configService,
            actionLogRepository,
            notificationClient);

    @Test
    void mapsUnhealthyAndDegradedCiHealthIntoOperationalActions() {
        writebackLogService.health = List.of(
                health("github-checks", "GITHUB", "HEALTHY", "SUCCESS", null),
                health("gitlab-merge-request", "GITLAB", "UNHEALTHY", "FAILED", null),
                health("jenkins-pipeline", "JENKINS", "DEGRADED", "SUCCESS", "BUILDING"),
                health("gitee-status", "GITEE", "NO_DATA", null, null));

        List<OperationsCiHealthActionVO> actions = service.listActions();

        assertEquals(50, writebackLogService.lastLimit);
        assertEquals(3, actions.size());
        assertEquals("CRITICAL", actions.get(0).getSeverity());
        assertEquals(4L, actions.get(0).getSlaHours());
        assertEquals("GitLab Owner", actions.get(0).getOwnerRole());
        assertEquals("CI Owner", actions.get(1).getOwnerRole());
        assertEquals("SUCCESS / BUILDING", actions.get(1).getLatestSignal());
        assertEquals(12L, actions.get(1).getLatestWritebackId());
        assertEquals("SUCCESS", actions.get(1).getLatestWritebackStatus());
        assertEquals("https://jenkins.example.com/job/review-agent/12/", actions.get(1).getLatestExternalBuildUrl());
        assertEquals("INFO", actions.get(2).getSeverity());
        assertEquals(72L, actions.get(2).getSlaHours());
    }

    @Test
    void skipsNotificationWhenWebhookIsMissing() {
        writebackLogService.health = List.of(health("jenkins-pipeline", "JENKINS", "DEGRADED", "SUCCESS", "BUILDING"));

        IntegrationActionLogVO result = service.notifyAction("jenkins-pipeline-DEGRADED");

        assertEquals("SKIPPED", result.getActionStatus());
        assertEquals("CI_HEALTH_NOTIFICATION", result.getActionType());
        assertEquals("ci-health:jenkins-pipeline:DEGRADED", result.getTargetKey());
        assertEquals(0, notificationClient.calls);
        assertEquals("Notification webhook is not configured", actionLogRepository.saved.get(0).getErrorMessage());
    }

    @Test
    void postsNotificationAndRecordsActionLog() {
        writebackLogService.health = List.of(health("gitlab-merge-request", "GITLAB", "UNHEALTHY", "FAILED", null));
        configService.notificationWebhookUrl = "https://hooks.example.com/ci-health";

        IntegrationActionLogVO result = service.notifyAction("gitlab-merge-request-UNHEALTHY");

        assertEquals("POSTED", result.getActionStatus());
        assertEquals("https://hooks.example.com/ci-health", result.getRequestUrl());
        assertEquals(1, notificationClient.calls);
        assertEquals("https://hooks.example.com/ci-health", notificationClient.lastWebhookUrl);
        assertEquals("gitlab-merge-request", notificationClient.lastAction.getConnectorKey());
        assertEquals("CI_HEALTH_NOTIFICATION", actionLogRepository.saved.get(0).getActionType());
    }

    private CiIntegrationHealthVO health(
            String connectorKey,
            String provider,
            String healthStatus,
            String latestWritebackStatus,
            String latestExternalResult) {
        CiIntegrationHealthVO vo = new CiIntegrationHealthVO();
        vo.setConnectorKey(connectorKey);
        vo.setProvider(provider);
        vo.setHealthStatus(healthStatus);
        vo.setLatestWritebackStatus(latestWritebackStatus);
        vo.setLatestExternalResult(latestExternalResult);
        if ("jenkins-pipeline".equals(connectorKey)) {
            vo.setLatestWritebackId(12L);
            vo.setLatestExternalBuildUrl("https://jenkins.example.com/job/review-agent/12/");
        }
        return vo;
    }

    private static class FakeCiStatusWritebackLogService implements CiStatusWritebackLogService {
        private int lastLimit;
        private List<CiIntegrationHealthVO> health = List.of();

        @Override
        public List<com.review.agent.domain.dto.CiStatusWritebackLogVO> listRecent(int limit) {
            return List.of();
        }

        @Override
        public List<CiIntegrationHealthVO> listHealth(int limit) {
            lastLimit = limit;
            return health;
        }

        @Override
        public void recordSuccess(Long reviewId, String commitSha, String state, String requestUrl) {
        }

        @Override
        public void recordSuccess(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl) {
        }

        @Override
        public void recordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        }

        @Override
        public void recordFailure(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        }

        @Override
        public void recordSkipped(Long reviewId, String state, String reason) {
        }

        @Override
        public void recordSkipped(String connectorKey, String provider, Long reviewId, String state, String reason) {
        }
    }

    private static class FakeCiStatusConfigService implements CiStatusConfigService {
        private String notificationWebhookUrl;

        @Override
        public CiStatusConfigVO getConfig(String connectorKey) {
            CiStatusConfigVO vo = new CiStatusConfigVO();
            vo.setConnectorKey(connectorKey);
            vo.setNotificationWebhookUrl(notificationWebhookUrl);
            return vo;
        }

        @Override
        public CiStatusConfigVO upsertConfig(UpsertCiStatusConfigRequest request) {
            return null;
        }
    }

    private static class FakeIntegrationActionLogRepository implements IntegrationActionLogRepository {
        private final List<IntegrationActionLog> saved = new ArrayList<>();

        @Override
        public List<IntegrationActionLog> listRecent(int limit) {
            return List.of();
        }

        @Override
        public void save(IntegrationActionLog log) {
            saved.add(log);
        }
    }

    private static class FakeNotificationClient implements OperationsCiHealthNotificationClient {
        private int calls;
        private String lastWebhookUrl;
        private OperationsCiHealthActionVO lastAction;

        @Override
        public void send(String webhookUrl, OperationsCiHealthActionVO action) {
            calls++;
            lastWebhookUrl = webhookUrl;
            lastAction = action;
        }
    }
}
