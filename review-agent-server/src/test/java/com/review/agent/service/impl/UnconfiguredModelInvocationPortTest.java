package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelInvocationRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnconfiguredModelInvocationPortTest {

    private final UnconfiguredModelInvocationPort port = new UnconfiguredModelInvocationPort();

    @Test
    void failsFastWithActionableMessageWhenNoRealModelPortIsConfigured() {
        ModelInvocationRequest request = new ModelInvocationRequest();
        request.setStrategyKey("quality-gate");
        request.setProvider("DashScope");
        request.setModelName("qwen-max");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> port.invoke(request));

        assertEquals("No readable ModelInvocationPort is configured for strategy quality-gate using DashScope/qwen-max. Implement a real Gateway or Agent adapter and wrap it with TelemetryModelInvocationPort.", thrown.getMessage());
    }
}
