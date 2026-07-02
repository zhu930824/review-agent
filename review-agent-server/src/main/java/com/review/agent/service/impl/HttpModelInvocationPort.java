package com.review.agent.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;
import com.review.agent.service.ModelInvocationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class HttpModelInvocationPort implements ModelInvocationPort {

    private final RestTemplate restTemplate;
    private final HttpModelInvocationProperties properties;

    @Override
    public ModelInvocationResponse invoke(ModelInvocationRequest request) {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalStateException("review-agent.model-invocation.http.endpoint must be configured when HTTP model invocation is enabled.");
        }

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                properties.getEndpoint(),
                new HttpEntity<>(requestBody(request), headers()),
                JsonNode.class);
        return response(response.getBody());
    }

    private Map<String, Object> requestBody(ModelInvocationRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelName(request));
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", request == null ? "" : defaultString(request.getPrompt()))));
        if (request != null && request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        return body;
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getApiKey()) && StringUtils.hasText(properties.getApiKeyHeader())) {
            headers.set(properties.getApiKeyHeader(), defaultString(properties.getApiKeyPrefix()) + properties.getApiKey());
        }
        return headers;
    }

    private ModelInvocationResponse response(JsonNode body) {
        ModelInvocationResponse response = new ModelInvocationResponse();
        response.setContent(textAt(body, properties.getResponseTextPointer()));
        Integer promptTokens = intAt(body, properties.getPromptTokensPointer());
        Integer completionTokens = intAt(body, properties.getCompletionTokensPointer());
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        response.setCostMicroCents(cost(promptTokens, completionTokens));
        return response;
    }

    private String modelName(ModelInvocationRequest request) {
        if (request != null && StringUtils.hasText(request.getModelName())) {
            return request.getModelName();
        }
        return properties.getModelName();
    }

    private String textAt(JsonNode body, String pointer) {
        if (body == null || !StringUtils.hasText(pointer)) {
            return null;
        }
        JsonNode node = body.at(pointer);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private Integer intAt(JsonNode body, String pointer) {
        if (body == null || !StringUtils.hasText(pointer)) {
            return null;
        }
        JsonNode node = body.at(pointer);
        return node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private Integer cost(Integer promptTokens, Integer completionTokens) {
        long promptCost = tokenCost(promptTokens, properties.getPromptCostMicroCentsPerThousandTokens());
        long completionCost = tokenCost(completionTokens, properties.getCompletionCostMicroCentsPerThousandTokens());
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, promptCost + completionCost));
    }

    private long tokenCost(Integer tokens, Integer microCentsPerThousandTokens) {
        if (tokens == null || microCentsPerThousandTokens == null) {
            return 0L;
        }
        return Math.round(tokens * (microCentsPerThousandTokens / 1000.0d));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
