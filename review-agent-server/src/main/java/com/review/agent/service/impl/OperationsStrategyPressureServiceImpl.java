package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.dto.OperationsStrategyPressureVO;
import com.review.agent.service.ModelTelemetryService;
import com.review.agent.service.OperationsStrategyPressureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsStrategyPressureServiceImpl implements OperationsStrategyPressureService {

    private final ModelTelemetryService modelTelemetryService;

    @Override
    public List<OperationsStrategyPressureVO> listPressure() {
        List<ModelTelemetrySummaryVO.StrategyTelemetryVO> strategies = modelTelemetryService.summary().getStrategies();
        long maxCost = Math.max(1L, strategies.stream()
                .mapToLong(strategy -> defaultZero(strategy.getAvgCostMicroCents()))
                .max()
                .orElse(1L));

        return strategies.stream()
                .map(strategy -> toPressure(strategy, maxCost))
                .sorted(Comparator
                        .comparing(OperationsStrategyPressureVO::getPressureScore).reversed()
                        .thenComparing(OperationsStrategyPressureVO::getStrategyKey))
                .toList();
    }

    private OperationsStrategyPressureVO toPressure(ModelTelemetrySummaryVO.StrategyTelemetryVO strategy, long maxCost) {
        long lowConfirmationPenalty = Math.max(0L, 100L - defaultZero(strategy.getConfirmationRatePercent()));
        long costPressure = Math.round((defaultZero(strategy.getAvgCostMicroCents()) * 100D) / maxCost);
        long pressureScore = clamp(Math.round(
                defaultZero(strategy.getFailureRatePercent()) * 0.30
                        + defaultZero(strategy.getFalsePositiveProxyPercent()) * 0.25
                        + lowConfirmationPenalty * 0.25
                        + costPressure * 0.10
                        + latencyPressure(defaultZero(strategy.getAvgLatencyMs())) * 0.05
                        + defaultZero(strategy.getJudgeFailureRatePercent()) * 0.05
                        + Math.max(0L, 100L - defaultZero(strategy.getStrategyHitRatePercent())) * 0.05));

        OperationsStrategyPressureVO vo = new OperationsStrategyPressureVO();
        vo.setStrategyKey(strategy.getStrategyKey());
        vo.setTotalCalls(defaultZero(strategy.getTotalCalls()));
        vo.setFailureRatePercent(defaultZero(strategy.getFailureRatePercent()));
        vo.setAvgCostMicroCents(defaultZero(strategy.getAvgCostMicroCents()));
        vo.setAvgLatencyMs(defaultZero(strategy.getAvgLatencyMs()));
        vo.setConfirmationRatePercent(defaultZero(strategy.getConfirmationRatePercent()));
        vo.setFalsePositiveProxyPercent(defaultZero(strategy.getFalsePositiveProxyPercent()));
        vo.setStrategyHitRatePercent(defaultZero(strategy.getStrategyHitRatePercent()));
        vo.setCrossHitRatePercent(defaultZero(strategy.getCrossHitRatePercent()));
        vo.setJudgeFailureRatePercent(defaultZero(strategy.getJudgeFailureRatePercent()));
        vo.setPressureScore(pressureScore);
        vo.setPressureLevel(pressureLevel(pressureScore));
        vo.setRecommendation(recommendation(strategy, pressureScore));
        return vo;
    }

    private long latencyPressure(long avgLatencyMs) {
        if (avgLatencyMs >= 3000) return 100L;
        if (avgLatencyMs >= 1500) return 70L;
        if (avgLatencyMs >= 800) return 40L;
        return 15L;
    }

    private String pressureLevel(long score) {
        if (score >= 60) return "HIGH";
        if (score >= 35) return "MEDIUM";
        return "LOW";
    }

    private String recommendation(ModelTelemetrySummaryVO.StrategyTelemetryVO strategy, long score) {
        if (score >= 60) {
            return "优先降噪并复核成本口径，必要时缩小适用场景。";
        }
        if (defaultZero(strategy.getJudgeFailureRatePercent()) > 0) {
            return "复核 Judge 调用稳定性，确认裁决链路是否需要降级兜底。";
        }
        if (defaultZero(strategy.getConfirmationRatePercent()) < 50) {
            return "补充人工样本复核，确认策略是否需要调参。";
        }
        if (defaultZero(strategy.getStrategyHitRatePercent()) < 50) {
            return "策略命中偏低，建议检查适用场景和触发条件。";
        }
        return "持续观察成本和质量趋势，保留当前策略。";
    }

    private long clamp(long value) {
        return Math.max(0L, Math.min(100L, value));
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
