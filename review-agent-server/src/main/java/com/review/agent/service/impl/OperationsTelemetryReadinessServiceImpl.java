package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.dto.OperationsTelemetryReadinessVO;
import com.review.agent.service.ModelTelemetryService;
import com.review.agent.service.OperationsTelemetryReadinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsTelemetryReadinessServiceImpl implements OperationsTelemetryReadinessService {

    private final ModelTelemetryService modelTelemetryService;

    @Override
    public List<OperationsTelemetryReadinessVO> listReadiness() {
        List<ModelTelemetrySummaryVO.StrategyTelemetryVO> strategies = modelTelemetryService.summary().getStrategies();
        if (strategies.isEmpty()) {
            return List.of(notConnected());
        }
        return strategies.stream()
                .map(this::toReadiness)
                .sorted(Comparator
                        .comparingInt(this::riskRank)
                        .thenComparing(OperationsTelemetryReadinessVO::getStrategyKey))
                .toList();
    }

    private OperationsTelemetryReadinessVO notConnected() {
        OperationsTelemetryReadinessVO vo = new OperationsTelemetryReadinessVO();
        vo.setStrategyKey("model-telemetry");
        vo.setReadinessLevel("NOT_CONNECTED");
        vo.setGapCode("NO_TELEMETRY");
        vo.setRecommendation(noTelemetryRecommendation());
        return vo;
    }

    private OperationsTelemetryReadinessVO toReadiness(ModelTelemetrySummaryVO.StrategyTelemetryVO strategy) {
        OperationsTelemetryReadinessVO vo = new OperationsTelemetryReadinessVO();
        vo.setStrategyKey(strategy.getStrategyKey());
        vo.setTotalCalls(defaultZero(strategy.getTotalCalls()));
        vo.setReviewedReviews(defaultZero(strategy.getReviewedReviews()));
        vo.setModelDiversity(defaultZero(strategy.getModelDiversity()));
        vo.setCrossHitRatePercent(defaultZero(strategy.getCrossHitRatePercent()));
        vo.setJudgeFailureRatePercent(defaultZero(strategy.getJudgeFailureRatePercent()));
        applyReadiness(vo);
        return vo;
    }

    private void applyReadiness(OperationsTelemetryReadinessVO vo) {
        if (vo.getTotalCalls() == 0) {
            vo.setReadinessLevel("NOT_CONNECTED");
            vo.setGapCode("NO_TELEMETRY");
            vo.setRecommendation(noTelemetryRecommendation());
            return;
        }
        if (vo.getJudgeFailureRatePercent() > 0) {
            vo.setReadinessLevel("JUDGE_UNSTABLE");
            vo.setGapCode("JUDGE_FAILURE");
            vo.setRecommendation("Review Judge call failures before using this strategy as a release gate.");
            return;
        }
        if (vo.getReviewedReviews() == 0 || vo.getModelDiversity() < 2 || vo.getCrossHitRatePercent() == 0) {
            vo.setReadinessLevel("NEEDS_ATTRIBUTION");
            vo.setGapCode("WEAK_ATTRIBUTION");
            vo.setRecommendation("Add review ids and multi-model attribution so findings can be traced back to model roles.");
            return;
        }
        vo.setReadinessLevel("READY");
        vo.setGapCode("NONE");
        vo.setRecommendation("Telemetry, model diversity, and attribution signals are available.");
    }

    private int riskRank(OperationsTelemetryReadinessVO vo) {
        if ("JUDGE_UNSTABLE".equals(vo.getReadinessLevel())) return 0;
        if ("NEEDS_ATTRIBUTION".equals(vo.getReadinessLevel())) return 1;
        if ("NOT_CONNECTED".equals(vo.getReadinessLevel())) return 2;
        return 3;
    }

    private String noTelemetryRecommendation() {
        return "Replace UnconfiguredModelInvocationPort with a readable Gateway or Agent adapter, then wrap it with TelemetryModelInvocationPort before relying on strategy operations metrics.";
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
