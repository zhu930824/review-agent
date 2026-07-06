package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationActionLogServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final IntegrationActionLogServiceImpl service = new IntegrationActionLogServiceImpl(repository);

    @Test
    void listRecentClampsLimitAndMapsRecords() {
        repository.logs = List.of(log("PR_SUMMARY_COMMENT", "FAILED", "17"));

        List<IntegrationActionLogVO> result = service.listRecent(200);

        assertEquals(50, repository.limit);
        assertEquals("PR_SUMMARY_COMMENT", result.get(0).getActionType());
        assertEquals("FAILED", result.get(0).getActionStatus());
        assertEquals("17", result.get(0).getTargetKey());
    }

    private IntegrationActionLog log(String actionType, String actionStatus, String targetKey) {
        IntegrationActionLog log = new IntegrationActionLog();
        log.setActionType(actionType);
        log.setActionStatus(actionStatus);
        log.setTargetKey(targetKey);
        return log;
    }

    private static class FakeRepository implements IntegrationActionLogRepository {
        private int limit;
        private List<IntegrationActionLog> logs = List.of();

        @Override
        public List<IntegrationActionLog> listRecent(int limit) {
            this.limit = limit;
            return logs;
        }

        @Override
        public void save(IntegrationActionLog log) {
        }
    }
}
