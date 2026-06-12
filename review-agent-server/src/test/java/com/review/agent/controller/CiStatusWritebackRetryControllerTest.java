package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.service.CiStatusConfigService;
import com.review.agent.service.CiStatusWritebackLogService;
import com.review.agent.service.CiStatusWritebackRetryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CiStatusWritebackRetryControllerTest {

    private final FakeRetryService retryService = new FakeRetryService();
    private final CiStatusConfigController controller = new CiStatusConfigController(
            nullCiStatusConfigService(),
            nullCiStatusWritebackLogService(),
            retryService);

    @Test
    void retryWritebackDelegatesToRetryService() {
        Result<Void> result = controller.retryWriteback(7L);

        assertTrue(result.isSuccess());
        assertEquals(7L, retryService.retriedLogId);
    }

    private CiStatusConfigService nullCiStatusConfigService() {
        return null;
    }

    private CiStatusWritebackLogService nullCiStatusWritebackLogService() {
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
}
