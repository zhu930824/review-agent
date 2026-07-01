package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModelCallTelemetryVO {

    private Long id;
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
    private Integer totalTokens;
    private Integer costMicroCents;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
