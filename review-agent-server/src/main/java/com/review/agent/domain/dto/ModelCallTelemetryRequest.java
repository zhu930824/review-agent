package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ModelCallTelemetryRequest {

    private Long reviewId;
    private String strategyKey;
    private String provider;
    private String modelName;
    private String role;
    private String promptVersion;
    private String status;
    private Integer latencyMs;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer costMicroCents;
    private String errorMessage;
}
