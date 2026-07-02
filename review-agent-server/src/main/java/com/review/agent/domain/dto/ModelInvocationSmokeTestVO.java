package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ModelInvocationSmokeTestVO {

    private String status;
    private String provider;
    private String modelName;
    private String promptVersion;
    private String content;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer costMicroCents;
    private String errorMessage;
}
