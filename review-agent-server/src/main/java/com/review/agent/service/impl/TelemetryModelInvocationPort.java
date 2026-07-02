package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.domain.dto.ModelTelemetryContext;
import com.review.agent.domain.dto.ModelTelemetryUsage;
import com.review.agent.service.ModelInvocationPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TelemetryModelInvocationPort implements ModelInvocationPort {

    private final ModelInvocationPort delegate;
    private final ModelTelemetryRecorder telemetryRecorder;

    @Override
    public ModelInvocationResponse invoke(ModelInvocationRequest request) throws Exception {
        return telemetryRecorder.recordCall(
                telemetryContext(request),
                () -> delegate.invoke(request),
                this::telemetryUsage);
    }

    private ModelTelemetryContext telemetryContext(ModelInvocationRequest request) {
        ModelTelemetryContext context = new ModelTelemetryContext();
        if (request != null) {
            context.setReviewId(request.getReviewId());
            context.setStrategyKey(request.getStrategyKey());
            context.setProvider(request.getProvider());
            context.setModelName(request.getModelName());
            context.setRole(request.getRole());
            context.setPromptVersion(request.getPromptVersion());
        }
        return context;
    }

    private ModelTelemetryUsage telemetryUsage(ModelInvocationResponse response) {
        ModelTelemetryUsage usage = new ModelTelemetryUsage();
        if (response != null) {
            usage.setPromptTokens(response.getPromptTokens());
            usage.setCompletionTokens(response.getCompletionTokens());
            usage.setCostMicroCents(response.getCostMicroCents());
        }
        return usage;
    }
}
