package com.review.agent.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiStatusWritebackRetrySchedulerTest {

    private final RecordingRetryService retryService = new RecordingRetryService();
    private final CiStatusWritebackRetryScheduler scheduler = new CiStatusWritebackRetryScheduler(retryService);

    @Test
    void delegatesScheduledRetryToService() {
        scheduler.retryDueWritebacks();

        assertEquals(1, retryService.retryDueCalls);
    }

    private static class RecordingRetryService implements CiStatusWritebackRetryService {
        private int retryDueCalls;

        @Override
        public void retry(Long writebackLogId) {
        }

        @Override
        public int retryDueWritebacks() {
            retryDueCalls++;
            return 2;
        }
    }
}
