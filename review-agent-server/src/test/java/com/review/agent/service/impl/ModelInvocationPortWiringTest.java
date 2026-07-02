package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.service.ModelInvocationPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ModelInvocationPortWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(UnconfiguredPortConfiguration.class);

    @Test
    void usesUnconfiguredPortWhenNoRealAdapterExists() {
        contextRunner.run(context -> {
            assertEquals(1, context.getBeansOfType(ModelInvocationPort.class).size());
            assertInstanceOf(UnconfiguredModelInvocationPort.class, context.getBean(ModelInvocationPort.class));
        });
    }

    @Test
    void realAdapterOverridesUnconfiguredFallback() {
        new ApplicationContextRunner()
                .withBean(ModelInvocationPort.class, FakeRealModelInvocationPort::new)
                .withUserConfiguration(UnconfiguredPortConfiguration.class)
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(ModelInvocationPort.class).size());
                    assertInstanceOf(FakeRealModelInvocationPort.class, context.getBean(ModelInvocationPort.class));
                });
    }

    @Configuration
    @Import(UnconfiguredModelInvocationPort.class)
    static class UnconfiguredPortConfiguration {
    }

    static class FakeRealModelInvocationPort implements ModelInvocationPort {

        @Override
        public ModelInvocationResponse invoke(ModelInvocationRequest request) {
            ModelInvocationResponse response = new ModelInvocationResponse();
            response.setContent("real adapter");
            return response;
        }
    }
}
