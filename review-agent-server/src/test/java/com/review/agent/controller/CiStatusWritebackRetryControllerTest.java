package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.CiStatusWritebackLogVO;
import com.review.agent.service.CiStatusConfigService;
import com.review.agent.service.CiStatusWritebackLogService;
import com.review.agent.service.CiStatusWritebackRetryService;
import com.review.agent.service.JenkinsBuildResultRefreshService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CiStatusWritebackRetryControllerTest {

    private final FakeRetryService retryService = new FakeRetryService();
    private final FakeWritebackLogService writebackLogService = new FakeWritebackLogService();
    private final FakeJenkinsRefreshService jenkinsRefreshService = new FakeJenkinsRefreshService();
    private final CiStatusConfigController controller = new CiStatusConfigController(
            nullCiStatusConfigService(),
            writebackLogService,
            retryService,
            jenkinsRefreshService);

    @Test
    void retryWritebackDelegatesToRetryService() {
        Result<Void> result = controller.retryWriteback(7L);

        assertTrue(result.isSuccess());
        assertEquals(7L, retryService.retriedLogId);
    }

    @Test
    void refreshJenkinsWritebackResultsDelegatesToRefreshService() {
        Result<Integer> result = controller.refreshJenkinsWritebackResults(5);

        assertTrue(result.isSuccess());
        assertEquals(5, jenkinsRefreshService.lastLimit);
        assertEquals(2, result.getData());
    }

    @Test
    void listWritebackHealthDelegatesToWritebackLogService() {
        Result<List<CiIntegrationHealthVO>> result = controller.listWritebackHealth(30);

        assertTrue(result.isSuccess());
        assertEquals(30, writebackLogService.lastHealthLimit);
        assertEquals("github-checks", result.getData().getFirst().getConnectorKey());
    }

    private CiStatusConfigService nullCiStatusConfigService() {
        return null;
    }

    private static class FakeRetryService implements CiStatusWritebackRetryService {
        private Long retriedLogId;

        @Override
        public void retry(Long writebackLogId) {
            this.retriedLogId = writebackLogId;
        }

        @Override
        public int retryDueWritebacks() {
            return 0;
        }
    }

    private static class FakeJenkinsRefreshService implements JenkinsBuildResultRefreshService {
        private int lastLimit;

        @Override
        public int refreshRecent(int limit) {
            lastLimit = limit;
            return 2;
        }
    }

    private static class FakeWritebackLogService implements CiStatusWritebackLogService {
        private int lastHealthLimit;

        @Override
        public List<CiStatusWritebackLogVO> listRecent(int limit) {
            return List.of();
        }

        @Override
        public List<CiIntegrationHealthVO> listHealth(int limit) {
            lastHealthLimit = limit;
            CiIntegrationHealthVO health = new CiIntegrationHealthVO();
            health.setConnectorKey("github-checks");
            health.setProvider("GITHUB");
            health.setHealthStatus("HEALTHY");
            return List.of(health);
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
