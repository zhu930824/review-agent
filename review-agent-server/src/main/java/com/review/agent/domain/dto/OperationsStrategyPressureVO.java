package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationsStrategyPressureVO {

    private String strategyKey;
    private Long totalCalls = 0L;
    private Long failureRatePercent = 0L;
    private Long avgCostMicroCents = 0L;
    private Long avgLatencyMs = 0L;
    private Long confirmationRatePercent = 0L;
    private Long falsePositiveProxyPercent = 0L;
    private Long strategyHitRatePercent = 0L;
    private Long crossHitRatePercent = 0L;
    private Long judgeFailureRatePercent = 0L;
    private Long pressureScore = 0L;
    private String pressureLevel;
    private String recommendation;
}
