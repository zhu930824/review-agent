package com.review.agent.service.impl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "review-agent.model-invocation.http")
public class HttpModelInvocationProperties {

    private boolean enabled = false;
    private String endpoint;
    private String apiKey;
    private String apiKeyHeader = "Authorization";
    private String apiKeyPrefix = "Bearer ";
    private String provider = "OpenAI-Compatible";
    private String modelName;
    private String promptVersion = "http-chat-completions-v1";
    private String responseTextPointer = "/choices/0/message/content";
    private String promptTokensPointer = "/usage/prompt_tokens";
    private String completionTokensPointer = "/usage/completion_tokens";
    private Integer promptCostMicroCentsPerThousandTokens = 0;
    private Integer completionCostMicroCentsPerThousandTokens = 0;
    private int connectTimeoutMillis = 10000;
    private int readTimeoutMillis = 60000;
}
