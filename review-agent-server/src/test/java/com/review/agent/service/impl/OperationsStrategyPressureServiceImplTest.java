package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.dto.OperationsStrategyPressureVO;
import com.review.agent.service.ModelTelemetryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsStrategyPressureServiceImplTest {

    private final FakeModelTelemetryService telemetryService = new FakeModelTelemetryService();
    private final OperationsStrategyPressureServiceImpl service = new OperationsStrategyPressureServiceImpl(telemetryService);

    @Test
    void listPressureRanksNoisyCostlyStrategiesFirst() {
        telemetryService.summary.getStrategies().add(strategy("fast-scan", 0, 90, 11, 80, 90, 450, 0, 20));
        telemetryService.summary.getStrategies().add(strategy("quality-gate", 25, 900, 75, 17, 67, 1800, 100, 12));

        List<OperationsStrategyPressureVO> result = service.listPressure();

        assertEquals("quality-gate", result.get(0).getStrategyKey());
        assertEquals("HIGH", result.get(0).getPressureLevel());
        assertTrue(result.get(0).getPressureScore() > result.get(1).getPressureScore());
        assertTrue(result.get(0).getRecommendation().contains("降噪"));
    }

    private ModelTelemetrySummaryVO.StrategyTelemetryVO strategy(
            String key,
            long failureRate,
            long avgCost,
            long falsePositive,
            long confirmation,
            long hitRate,
            long latency,
            long judgeFailureRate,
            long calls) {
        ModelTelemetrySummaryVO.StrategyTelemetryVO vo = new ModelTelemetrySummaryVO.StrategyTelemetryVO();
        vo.setStrategyKey(key);
        vo.setTotalCalls(calls);
        vo.setFailureRatePercent(failureRate);
        vo.setAvgCostMicroCents(avgCost);
        vo.setFalsePositiveProxyPercent(falsePositive);
        vo.setConfirmationRatePercent(confirmation);
        vo.setStrategyHitRatePercent(hitRate);
        vo.setAvgLatencyMs(latency);
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
