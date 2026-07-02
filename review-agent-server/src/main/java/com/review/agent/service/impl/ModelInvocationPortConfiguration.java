package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.service.ModelInvocationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(HttpModelInvocationProperties.class)
public class ModelInvocationPortConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "review-agent.model-invocation.http", name = "enabled", havingValue = "true")
    public ModelInvocationPort httpTelemetryModelInvocationPort(
            RestTemplateBuilder restTemplateBuilder,
            HttpModelInvocationProperties properties,
            ModelTelemetryRecorder telemetryRecorder) {
        HttpModelInvocationPort httpPort = new HttpModelInvocationPort(
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                        .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                        .build(),
                properties);
        return new DefaultingModelInvocationPort(
                new TelemetryModelInvocationPort(httpPort, telemetryRecorder),
                properties);
    }

    private static class DefaultingModelInvocationPort implements ModelInvocationPort {

        private final ModelInvocationPort delegate;
        private final HttpModelInvocationProperties properties;

        private DefaultingModelInvocationPort(ModelInvocationPort delegate, HttpModelInvocationProperties properties) {
            this.delegate = delegate;
            this.properties = properties;
        }

        @Override
        public ModelInvocationResponse invoke(ModelInvocationRequest request) throws Exception {
            return delegate.invoke(normalized(request));
        }

        private ModelInvocationRequest normalized(ModelInvocationRequest request) {
            ModelInvocationRequest normalized = new ModelInvocationRequest();
            if (request != null) {
                normalized.setReviewId(request.getReviewId());
                normalized.setStrategyKey(request.getStrategyKey());
                normalized.setProvider(request.getProvider());
                normalized.setModelName(request.getModelName());
                normalized.setRole(request.getRole());
                normalized.setPromptVersion(request.getPromptVersion());
                normalized.setPrompt(request.getPrompt());
                normalized.setTemperature(request.getTemperature());
            }
            if (!StringUtils.hasText(normalized.getProvider())) {
                normalized.setProvider(properties.getProvider());
            }
            if (!StringUtils.hasText(normalized.getModelName())) {
                normalized.setModelName(properties.getModelName());
            }
            if (!StringUtils.hasText(normalized.getPromptVersion())) {
                normalized.setPromptVersion(properties.getPromptVersion());
            }
            return normalized;
        }
    }
}
