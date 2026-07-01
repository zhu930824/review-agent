package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelTelemetryContext;
import com.review.agent.domain.dto.ModelTelemetryUsage;
import com.review.agent.service.ModelTelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ModelTelemetryRecorder {

    private final ModelTelemetryService modelTelemetryService;

    public <T> T recordCall(
            ModelTelemetryContext context,
            Callable<T> operation,
            Function<T, ModelTelemetryUsage> usageExtractor) throws Exception {
        long startedAt = System.nanoTime();
        try {
            T result = operation.call();
            record(context, "SUCCESS", elapsedMs(startedAt), usage(usageExtractor, result), null);
            return result;
        } catch (Exception e) {
            record(context, "FAILED", elapsedMs(startedAt), new ModelTelemetryUsage(), e.getMessage());
            throw e;
        }
    }

    private <T> ModelTelemetryUsage usage(Function<T, ModelTelemetryUsage> usageExtractor, T result) {
        if (usageExtractor == null) {
            return new ModelTelemetryUsage();
        }
        ModelTelemetryUsage usage = usageExtractor.apply(result);
        return usage == null ? new ModelTelemetryUsage() : usage;
    }

    private void record(
            ModelTelemetryContext context,
            String status,
            int latencyMs,
            ModelTelemetryUsage usage,
            String errorMessage) {
        ModelCallTelemetryRequest request = new ModelCallTelemetryRequest();
        if (context != null) {
            request.setReviewId(context.getReviewId());
            request.setStrategyKey(context.getStrategyKey());
            request.setProvider(context.getProvider());
            request.setModelName(context.getModelName());
            request.setRole(context.getRole());
            request.setPromptVersion(context.getPromptVersion());
        }
        request.setStatus(status);
        request.setLatencyMs(latencyMs);
        request.setPromptTokens(defaultZero(usage.getPromptTokens()));
        request.setCompletionTokens(defaultZero(usage.getCompletionTokens()));
        request.setCostMicroCents(defaultZero(usage.getCostMicroCents()));
        request.setErrorMessage(errorMessage);
        modelTelemetryService.record(request);
    }

    private int elapsedMs(long startedAt) {
        return Math.max(0, Math.toIntExact((System.nanoTime() - startedAt) / 1_000_000L));
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
