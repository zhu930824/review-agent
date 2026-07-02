package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.dto.OperationsTelemetryReadinessVO;
import com.review.agent.service.ModelTelemetryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsTelemetryReadinessServiceImplTest {

    private final FakeModelTelemetryService telemetryService = new FakeModelTelemetryService();
    private final OperationsTelemetryReadinessServiceImpl service = new OperationsTelemetryReadinessServiceImpl(telemetryService);

    @Test
    void reportsNotConnectedWhenNoTelemetryExists() {
        List<OperationsTelemetryReadinessVO> result = service.listReadiness();

        assertEquals(1, result.size());
        assertEquals("model-telemetry", result.get(0).getStrategyKey());
        assertEquals("NOT_CONNECTED", result.get(0).getReadinessLevel());
        assertEquals("NO_TELEMETRY", result.get(0).getGapCode());
        assertEquals("Replace UnconfiguredModelInvocationPort with a readable Gateway or Agent adapter, then wrap it with TelemetryModelInvocationPort before relying on strategy operations metrics.",
                result.get(0).getRecommendation());
    }

    @Test
    void ranksAttributionAndJudgeGapsBeforeReadyStrategies() {
        telemetryService.summary.getStrategies().add(strategy("ready", 12, 5, 3, 70, 0));
        telemetryService.summary.getStrategies().add(strategy("single-model", 9, 4, 1, 0, 0));
        telemetryService.summary.getStrategies().add(strategy("judge-risk", 10, 4, 3, 60, 100));

        List<OperationsTelemetryReadinessVO> result = service.listReadiness();

        assertEquals(List.of("judge-risk", "single-model", "ready"), result.stream().map(OperationsTelemetryReadinessVO::getStrategyKey).toList());
        assertEquals("JUDGE_UNSTABLE", result.get(0).getReadinessLevel());
        assertEquals("NEEDS_ATTRIBUTION", result.get(1).getReadinessLevel());
        assertEquals("READY", result.get(2).getReadinessLevel());
    }

    private ModelTelemetrySummaryVO.StrategyTelemetryVO strategy(
            String key,
            long calls,
            long reviewedReviews,
            long modelDiversity,
            long crossHitRate,
            long judgeFailureRate) {
        ModelTelemetrySummaryVO.StrategyTelemetryVO vo = new ModelTelemetrySummaryVO.StrategyTelemetryVO();
        vo.setStrategyKey(key);
        vo.setTotalCalls(calls);
        vo.setReviewedReviews(reviewedReviews);
        vo.setModelDiversity(modelDiversity);
        vo.setCrossHitRatePercent(crossHitRate);
        vo.setJudgeFailureRatePercent(judgeFailureRate);
        return vo;
    }

    private static class FakeModelTelemetryService implements ModelTelemetryService {
        private final ModelTelemetrySummaryVO summary = new ModelTelemetrySummaryVO();

        @Override
        public ModelCallTelemetryVO record(ModelCallTelemetryRequest request) {
            return new ModelCallTelemetryVO();
        }

        @Override
        public ModelTelemetrySummaryVO summary() {
            return summary;
        }
    }
}
