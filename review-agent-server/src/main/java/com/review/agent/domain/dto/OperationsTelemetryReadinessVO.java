package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationsTelemetryReadinessVO {

    private String strategyKey;

    private Long totalCalls = 0L;

    private Long reviewedReviews = 0L;

    private Long modelDiversity = 0L;

    private Long crossHitRatePercent = 0L;

    private Long judgeFailureRatePercent = 0L;

    private String readinessLevel;

    private String gapCode;

    private String recommendation;
}
