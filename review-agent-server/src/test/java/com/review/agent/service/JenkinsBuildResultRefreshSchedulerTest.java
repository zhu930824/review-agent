package com.review.agent.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JenkinsBuildResultRefreshSchedulerTest {

    private final RecordingRefreshService refreshService = new RecordingRefreshService();
    private final JenkinsBuildResultRefreshScheduler scheduler = new JenkinsBuildResultRefreshScheduler(refreshService);

    @Test
    void delegatesScheduledRefreshToServiceWithConfiguredLimit() {
        ReflectionTestUtils.setField(scheduler, "refreshLimit", 7);

        scheduler.refreshRecentJenkinsResults();

        assertEquals(7, refreshService.lastLimit);
        assertEquals(1, refreshService.calls);
    }

    private static class RecordingRefreshService implements JenkinsBuildResultRefreshService {
        private int calls;
        private int lastLimit;

        @Override
        public int refreshRecent(int limit) {
            calls++;
            lastLimit = limit;
            return 3;
        }
    }
}
