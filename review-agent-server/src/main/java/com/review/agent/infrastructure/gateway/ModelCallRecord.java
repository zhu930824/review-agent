package com.review.agent.infrastructure.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCallRecord {
    private String callId;
    private String promptKey;
    private String modelName;
    private String status;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer latencyMs;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
