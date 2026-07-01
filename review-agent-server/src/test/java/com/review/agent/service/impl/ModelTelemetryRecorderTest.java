package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetryContext;
import com.review.agent.domain.dto.ModelTelemetryUsage;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.service.ModelTelemetryService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelTelemetryRecorderTest {

    private final FakeModelTelemetryService telemetryService = new FakeModelTelemetryService();
    private final ModelTelemetryRecorder recorder = new ModelTelemetryRecorder(telemetryService);

    @Test
    void recordsSuccessfulModelCallAroundOperation() throws Exception {
        ModelTelemetryContext context = context();

        String result = recorder.recordCall(context, () -> "ok",
                value -> usage(120, 45, 80));

        assertEquals("ok", result);
        assertEquals(9001L, telemetryService.recorded.getReviewId());
        assertEquals("quality-gate", telemetryService.recorded.getStrategyKey());
        assertEquals("DashScope", telemetryService.recorded.getProvider());
        assertEquals("qwen-max", telemetryService.recorded.getModelName());
        assertEquals("SECURITY_AUDITOR", telemetryService.recorded.getRole());
        assertEquals("v3", telemetryService.recorded.getPromptVersion());
        assertEquals("SUCCESS", telemetryService.recorded.getStatus());
        assertEquals(120, telemetryService.recorded.getPromptTokens());
        assertEquals(45, telemetryService.recorded.getCompletionTokens());
        assertEquals(80, telemetryService.recorded.getCostMicroCents());
        assertEquals(1, telemetryService.recordCount.get());
    }

    @Test
    void recordsFailureAndRethrowsOriginalException() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> recorder.recordCall(context(), () -> {
                    throw new IllegalStateException("provider timeout");
                }, value -> usage(1, 1, 1)));

        assertEquals("provider timeout", thrown.getMessage());
        assertEquals("FAILED", telemetryService.recorded.getStatus());
        assertEquals("provider timeout", telemetryService.recorded.getErrorMessage());
        assertEquals(0, telemetryService.recorded.getPromptTokens());
        assertEquals(0, telemetryService.recorded.getCompletionTokens());
        assertEquals(0, telemetryService.recorded.getCostMicroCents());
    }

    private ModelTelemetryContext context() {
        ModelTelemetryContext context = new ModelTelemetryContext();
        context.setReviewId(9001L);
        context.setStrategyKey("quality-gate");
        context.setProvider("DashScope");
        context.setModelName("qwen-max");
        context.setRole("SECURITY_AUDITOR");
        context.setPromptVersion("v3");
        return context;
    }

    private ModelTelemetryUsage usage(Integer promptTokens, Integer completionTokens, Integer costMicroCents) {
        ModelTelemetryUsage usage = new ModelTelemetryUsage();
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setCostMicroCents(costMicroCents);
        return usage;
    }

    private static class FakeModelTelemetryService implements ModelTelemetryService {
        private final AtomicInteger recordCount = new AtomicInteger();
        private ModelCallTelemetryRequest recorded;

        @Override
        public ModelCallTelemetryVO record(ModelCallTelemetryRequest request) {
            recorded = request;
            recordCount.incrementAndGet();
            ModelCallTelemetryVO vo = new ModelCallTelemetryVO();
            vo.setStatus(request.getStatus());
            return vo;
        }

        @Override
        public ModelTelemetrySummaryVO summary() {
            return new ModelTelemetrySummaryVO();
        }
    }
}
