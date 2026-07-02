package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ModelInvocationSmokeTestRequest {

    private Long reviewId;
    private String strategyKey;
    private String provider;
    private String modelName;
    private String role;
    private String promptVersion;
    private String prompt;
    private Double temperature;
}
