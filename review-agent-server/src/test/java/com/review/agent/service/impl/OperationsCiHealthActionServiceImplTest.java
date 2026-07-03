package com.review.agent.service.impl;

import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.service.CiStatusWritebackLogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsCiHealthActionServiceImplTest {

    private final FakeCiStatusWritebackLogService writebackLogService = new FakeCiStatusWritebackLogService();
    private final OperationsCiHealthActionServiceImpl service = new OperationsCiHealthActionServiceImpl(writebackLogService);

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
        assertEquals("INFO", actions.get(2).getSeverity());
        assertEquals(72L, actions.get(2).getSlaHours());
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
}
