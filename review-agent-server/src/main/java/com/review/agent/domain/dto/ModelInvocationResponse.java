package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ModelInvocationResponse {

    private String content;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer costMicroCents;
}
