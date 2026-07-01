package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.service.ModelTelemetryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTelemetryControllerTest {

    private final FakeService service = new FakeService();
    private final ModelTelemetryController controller = new ModelTelemetryController(service);

    @Test
    void recordDelegatesToService() {
        ModelCallTelemetryRequest request = new ModelCallTelemetryRequest();
        request.setProvider("DashScope");
        request.setModelName("qwen-max");

        Result<ModelCallTelemetryVO> result = controller.record(request);

        assertTrue(result.isSuccess());
        assertEquals("DashScope", service.recorded.getProvider());
        assertEquals("qwen-max", result.getData().getModelName());
    }

    @Test
    void summaryDelegatesToService() {
        Result<ModelTelemetrySummaryVO> result = controller.summary();

        assertTrue(result.isSuccess());
        assertEquals(9, result.getData().getTotalCalls());
    }

    private static class FakeService implements ModelTelemetryService {
        private ModelCallTelemetryRequest recorded;

        @Override
        public ModelCallTelemetryVO record(ModelCallTelemetryRequest request) {
            recorded = request;
            ModelCallTelemetryVO vo = new ModelCallTelemetryVO();
            vo.setProvider(request.getProvider());
            vo.setModelName(request.getModelName());
            return vo;
        }

        @Override
        public ModelTelemetrySummaryVO summary() {
            ModelTelemetrySummaryVO vo = new ModelTelemetrySummaryVO();
            vo.setTotalCalls(9L);
            return vo;
        }
    }
}
