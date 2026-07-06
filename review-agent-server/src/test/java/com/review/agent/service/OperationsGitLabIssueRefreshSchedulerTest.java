package com.review.agent.service;

import com.review.agent.domain.dto.OperationsExternalIssueVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.dto.LinkOperationsExternalIssueRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsGitLabIssueRefreshSchedulerTest {

    private final RecordingTaskService taskService = new RecordingTaskService();
    private final OperationsGitLabIssueRefreshScheduler scheduler = new OperationsGitLabIssueRefreshScheduler(taskService);

    @Test
    void delegatesScheduledRefreshToServiceWithConfiguredLimit() {
        ReflectionTestUtils.setField(scheduler, "refreshLimit", 9);

        scheduler.refreshRecentGitLabIssues();

        assertEquals(9, taskService.lastLimit);
        assertEquals(1, taskService.calls);
    }

    private static class RecordingTaskService implements OperationsTaskService {
        private int calls;
        private int lastLimit;

        @Override
        public List<OperationsTaskVO> listTasks(int limit) {
            return List.of();
        }

        @Override
        public List<OperationsTaskVO> listSlaAlerts(int limit) {
            return List.of();
        }

        @Override
        public List<OperationsTaskVO> syncTasks(int limit) {
            return List.of();
        }

        @Override
        public void updateTask(String taskKey, String status, String ownerRole, Long slaHours) {
        }

        @Override
        public void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours) {
        }

        @Override
        public OperationsExternalIssueVO syncGitLabIssue(String taskKey) {
            return new OperationsExternalIssueVO();
        }

        @Override
        public OperationsExternalIssueVO refreshGitLabIssue(String taskKey) {
            return new OperationsExternalIssueVO();
        }

        @Override
        public int refreshRecentGitLabIssues(int limit) {
            calls++;
            lastLimit = limit;
            return limit;
        }

        @Override
        public OperationsExternalIssueVO linkExternalIssue(String taskKey, LinkOperationsExternalIssueRequest request) {
            return new OperationsExternalIssueVO();
        }

        @Override
        public void closeTask(String taskKey, String closeReason) {
        }
    }
}
