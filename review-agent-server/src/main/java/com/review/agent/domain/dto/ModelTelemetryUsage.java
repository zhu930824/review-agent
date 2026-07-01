package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ModelTelemetryUsage {

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer costMicroCents;
}
