package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.service.ModelInvocationPort;
import com.review.agent.service.ModelTelemetryService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryModelInvocationPortTest {

    private final FakeModelInvocationPort delegate = new FakeModelInvocationPort();
    private final FakeModelTelemetryService telemetryService = new FakeModelTelemetryService();
    private final TelemetryModelInvocationPort port = new TelemetryModelInvocationPort(
            delegate,
            new ModelTelemetryRecorder(telemetryService));

    @Test
    void recordsTelemetryAroundDelegatedInvocation() throws Exception {
        delegate.response = response("analysis ok", 320, 180, 90);

        ModelInvocationResponse result = port.invoke(request());

        assertEquals("analysis ok", result.getContent());
        assertEquals(1, delegate.callCount.get());
        assertEquals("Check security issues", delegate.request.getPrompt());
        assertEquals(77L, telemetryService.recorded.getReviewId());
        assertEquals("quality-gate", telemetryService.recorded.getStrategyKey());
        assertEquals("DashScope", telemetryService.recorded.getProvider());
        assertEquals("qwen-max", telemetryService.recorded.getModelName());
        assertEquals("SECURITY_AUDITOR", telemetryService.recorded.getRole());
        assertEquals("v4", telemetryService.recorded.getPromptVersion());
        assertEquals("SUCCESS", telemetryService.recorded.getStatus());
        assertEquals(320, telemetryService.recorded.getPromptTokens());
        assertEquals(180, telemetryService.recorded.getCompletionTokens());
        assertEquals(90, telemetryService.recorded.getCostMicroCents());
    }

    @Test
    void recordsFailedTelemetryAndRethrowsDelegateError() {
        delegate.error = new IllegalStateException("provider timeout");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> port.invoke(request()));

        assertEquals("provider timeout", thrown.getMessage());
        assertEquals(1, delegate.callCount.get());
        assertEquals("FAILED", telemetryService.recorded.getStatus());
        assertEquals("provider timeout", telemetryService.recorded.getErrorMessage());
        assertEquals(0, telemetryService.recorded.getPromptTokens());
        assertEquals(0, telemetryService.recorded.getCompletionTokens());
        assertEquals(0, telemetryService.recorded.getCostMicroCents());
    }

    private ModelInvocationRequest request() {
        ModelInvocationRequest request = new ModelInvocationRequest();
        request.setReviewId(77L);
        request.setStrategyKey("quality-gate");
        request.setProvider("DashScope");
        request.setModelName("qwen-max");
        request.setRole("SECURITY_AUDITOR");
        request.setPromptVersion("v4");
        request.setPrompt("Check security issues");
        request.setTemperature(0.2D);
        return request;
    }

    private ModelInvocationResponse response(
            String content,
            Integer promptTokens,
            Integer completionTokens,
            Integer costMicroCents) {
        ModelInvocationResponse response = new ModelInvocationResponse();
        response.setContent(content);
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        response.setCostMicroCents(costMicroCents);
        return response;
    }

    private static class FakeModelInvocationPort implements ModelInvocationPort {
        private final AtomicInteger callCount = new AtomicInteger();
        private ModelInvocationRequest request;
        private ModelInvocationResponse response;
        private RuntimeException error;

        @Override
        public ModelInvocationResponse invoke(ModelInvocationRequest request) {
            this.request = request;
            callCount.incrementAndGet();
            if (error != null) {
                throw error;
            }
            return response;
        }
    }

    private static class FakeModelTelemetryService implements ModelTelemetryService {
        private ModelCallTelemetryRequest recorded;

        @Override
        public ModelCallTelemetryVO record(ModelCallTelemetryRequest request) {
            recorded = request;
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
