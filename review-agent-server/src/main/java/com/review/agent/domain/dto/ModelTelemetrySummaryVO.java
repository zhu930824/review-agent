package com.review.agent.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelTelemetrySummaryVO {

    private Long totalCalls = 0L;
    private Long failedCalls = 0L;
    private Long totalTokens = 0L;
    private Long totalCostMicroCents = 0L;
    private Long avgLatencyMs = 0L;
    private Long failureRatePercent = 0L;
    private Long avgCostMicroCents = 0L;
    private List<StrategyTelemetryVO> strategies = new ArrayList<>();

    @Data
    public static class StrategyTelemetryVO {
        private String strategyKey;
        private Long totalCalls = 0L;
        private Long failedCalls = 0L;
        private Long totalTokens = 0L;
        private Long avgLatencyMs = 0L;
        private Long failureRatePercent = 0L;
        private Long avgCostMicroCents = 0L;
        private Long confirmedFindings = 0L;
        private Long dismissedFindings = 0L;
        private Long pendingFindings = 0L;
        private Long confirmationRatePercent = 0L;
        private Long falsePositiveProxyPercent = 0L;
        private Long reviewedReviews = 0L;
        private Long totalFindings = 0L;
        private Long strategyHitRatePercent = 0L;
        private Long findingsPerReview = 0L;
        private Long crossHitFindings = 0L;
        private Long crossHitRatePercent = 0L;
        private Long modelDiversity = 0L;
        private Long judgeCalls = 0L;
        private Long judgeFailureRatePercent = 0L;
    }
}
