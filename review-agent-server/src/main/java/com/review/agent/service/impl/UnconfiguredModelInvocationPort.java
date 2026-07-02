package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.service.ModelInvocationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "review-agent.model-invocation.http", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnconfiguredModelInvocationPort implements ModelInvocationPort {

    @Override
    public ModelInvocationResponse invoke(ModelInvocationRequest request) {
        throw new IllegalStateException(message(request));
    }

    private String message(ModelInvocationRequest request) {
        String strategyKey = request == null || request.getStrategyKey() == null ? "unknown-strategy" : request.getStrategyKey();
        String provider = request == null || request.getProvider() == null ? "unknown-provider" : request.getProvider();
        String modelName = request == null || request.getModelName() == null ? "unknown-model" : request.getModelName();
        return "No readable ModelInvocationPort is configured for strategy "
                + strategyKey
                + " using "
                + provider
                + "/"
                + modelName
                + ". Implement a real Gateway or Agent adapter and wrap it with TelemetryModelInvocationPort.";
    }
}
