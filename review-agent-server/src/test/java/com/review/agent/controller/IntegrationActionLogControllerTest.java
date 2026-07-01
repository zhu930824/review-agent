package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.service.IntegrationActionLogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationActionLogControllerTest {

    private final FakeService service = new FakeService();
    private final IntegrationActionLogController controller = new IntegrationActionLogController(service);

    @Test
    void listActionsDelegatesToService() {
        Result<List<IntegrationActionLogVO>> result = controller.listActions(12);

        assertTrue(result.isSuccess());
        assertEquals(12, service.limit);
        assertEquals("SARIF_UPLOAD", result.getData().get(0).getActionType());
    }

    private static class FakeService implements IntegrationActionLogService {
        private int limit;

        @Override
        public List<IntegrationActionLogVO> listRecent(int limit) {
            this.limit = limit;
            IntegrationActionLogVO vo = new IntegrationActionLogVO();
            vo.setActionType("SARIF_UPLOAD");
            return List.of(vo);
        }
    }
}
