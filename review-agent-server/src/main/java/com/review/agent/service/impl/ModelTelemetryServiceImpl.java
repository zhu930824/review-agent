package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.entity.ModelCallTelemetry;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.infrastructure.persistence.ModelTelemetryRepository;
import com.review.agent.service.ModelTelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelTelemetryServiceImpl implements ModelTelemetryService {

    private static final int SUMMARY_LIMIT = 500;

    private final ModelTelemetryRepository repository;

    @Override
    public ModelCallTelemetryVO record(ModelCallTelemetryRequest request) {
        ModelCallTelemetry telemetry = new ModelCallTelemetry();
        telemetry.setReviewId(request.getReviewId());
        telemetry.setStrategyKey(request.getStrategyKey());
        telemetry.setProvider(request.getProvider());
        telemetry.setModelName(request.getModelName());
        telemetry.setRole(request.getRole());
        telemetry.setPromptVersion(request.getPromptVersion());
        telemetry.setStatus(hasText(request.getStatus()) ? request.getStatus() : "SUCCESS");
        telemetry.setLatencyMs(defaultZero(request.getLatencyMs()));
        telemetry.setPromptTokens(defaultZero(request.getPromptTokens()));
        telemetry.setCompletionTokens(defaultZero(request.getCompletionTokens()));
        telemetry.setTotalTokens(defaultZero(request.getPromptTokens()) + defaultZero(request.getCompletionTokens()));
        telemetry.setCostMicroCents(defaultZero(request.getCostMicroCents()));
        telemetry.setErrorMessage(request.getErrorMessage());
        return toVO(repository.insert(telemetry));
    }

    @Override
    public ModelTelemetrySummaryVO summary() {
        List<ModelCallTelemetry> records = repository.listRecent(SUMMARY_LIMIT);
        Map<Long, List<ReviewFinding>> findingsByReviewId = findingsByReviewId(records);
        ModelTelemetrySummaryVO summary = new ModelTelemetrySummaryVO();
        summary.setTotalCalls((long) records.size());
        summary.setFailedCalls(records.stream().filter(this::isFailed).count());
        summary.setTotalTokens(records.stream().mapToLong(item -> defaultZero(item.getTotalTokens())).sum());
        summary.setTotalCostMicroCents(records.stream().mapToLong(item -> defaultZero(item.getCostMicroCents())).sum());
        summary.setAvgLatencyMs(averageLatency(records));
        summary.setFailureRatePercent(percent(summary.getFailedCalls(), summary.getTotalCalls()));
        summary.setAvgCostMicroCents(averageCost(records));
        summary.setStrategies(strategySummaries(records, findingsByReviewId));
        return summary;
    }

    private List<ModelTelemetrySummaryVO.StrategyTelemetryVO> strategySummaries(
            List<ModelCallTelemetry> records,
            Map<Long, List<ReviewFinding>> findingsByReviewId) {
        Map<String, List<ModelCallTelemetry>> byStrategy = new LinkedHashMap<>();
        records.stream()
                .sorted(Comparator.comparing(item -> valueOrDefault(item.getStrategyKey(), "unknown")))
                .forEach(item -> byStrategy
                        .computeIfAbsent(valueOrDefault(item.getStrategyKey(), "unknown"), ignored -> new java.util.ArrayList<>())
                        .add(item));

        return byStrategy.entrySet().stream()
                .map(entry -> {
                    ModelTelemetrySummaryVO.StrategyTelemetryVO vo = new ModelTelemetrySummaryVO.StrategyTelemetryVO();
                    List<ModelCallTelemetry> items = entry.getValue();
                    vo.setStrategyKey(entry.getKey());
                    vo.setTotalCalls((long) items.size());
                    vo.setFailedCalls(items.stream().filter(this::isFailed).count());
                    vo.setTotalTokens(items.stream().mapToLong(item -> defaultZero(item.getTotalTokens())).sum());
                    vo.setAvgLatencyMs(averageLatency(items));
                    vo.setFailureRatePercent(percent(vo.getFailedCalls(), vo.getTotalCalls()));
                    vo.setAvgCostMicroCents(averageCost(items));
                    applyQualityMetrics(vo, items, findingsByReviewId);
                    return vo;
                })
                .sorted(Comparator
                        .comparing(ModelTelemetrySummaryVO.StrategyTelemetryVO::getTotalCalls).reversed()
                        .thenComparing(ModelTelemetrySummaryVO.StrategyTelemetryVO::getStrategyKey))
                .toList();
    }

    private Map<Long, List<ReviewFinding>> findingsByReviewId(List<ModelCallTelemetry> records) {
        List<Long> reviewIds = records.stream()
                .map(ModelCallTelemetry::getReviewId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return repository.listFindingsByReviewIds(reviewIds).stream()
                .filter(finding -> finding.getReviewId() != null)
                .collect(Collectors.groupingBy(ReviewFinding::getReviewId));
    }

    private void applyQualityMetrics(
            ModelTelemetrySummaryVO.StrategyTelemetryVO vo,
            List<ModelCallTelemetry> records,
            Map<Long, List<ReviewFinding>> findingsByReviewId) {
        Set<Long> reviewIds = records.stream()
                .map(ModelCallTelemetry::getReviewId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        List<ReviewFinding> findings = reviewIds.stream()
                .flatMap(reviewId -> findingsByReviewId.getOrDefault(reviewId, Collections.emptyList()).stream())
                .toList();
        long confirmed = findings.stream().filter(finding -> HumanStatus.CONFIRMED == finding.getHumanStatus()).count();
        long dismissed = findings.stream().filter(finding -> HumanStatus.DISMISSED == finding.getHumanStatus()).count();
        long pending = findings.stream().filter(finding -> HumanStatus.PENDING == finding.getHumanStatus()).count();
        long crossHit = findings.stream().filter(finding -> Boolean.TRUE.equals(finding.getIsCrossHit())).count();
        long reviewsWithFindings = reviewIds.stream()
                .filter(reviewId -> !findingsByReviewId.getOrDefault(reviewId, Collections.emptyList()).isEmpty())
                .count();
        long judgeCalls = records.stream().filter(this::isJudgeCall).count();
        long judgeFailures = records.stream().filter(this::isJudgeCall).filter(this::isFailed).count();
        vo.setConfirmedFindings(confirmed);
        vo.setDismissedFindings(dismissed);
        vo.setPendingFindings(pending);
        vo.setConfirmationRatePercent(percent(confirmed, confirmed + dismissed + pending));
        vo.setFalsePositiveProxyPercent(percent(dismissed, confirmed + dismissed));
        vo.setReviewedReviews((long) reviewIds.size());
        vo.setTotalFindings((long) findings.size());
        vo.setStrategyHitRatePercent(percent(reviewsWithFindings, reviewIds.size()));
        vo.setFindingsPerReview(reviewIds.isEmpty() ? 0L : Math.round((findings.size() * 1D) / reviewIds.size()));
        vo.setCrossHitFindings(crossHit);
        vo.setCrossHitRatePercent(percent(crossHit, findings.size()));
        vo.setModelDiversity(records.stream()
                .map(ModelCallTelemetry::getModelName)
                .filter(this::hasText)
                .distinct()
                .count());
        vo.setJudgeCalls(judgeCalls);
        vo.setJudgeFailureRatePercent(percent(judgeFailures, judgeCalls));
    }

    private ModelCallTelemetryVO toVO(ModelCallTelemetry telemetry) {
        ModelCallTelemetryVO vo = new ModelCallTelemetryVO();
        vo.setId(telemetry.getId());
        vo.setReviewId(telemetry.getReviewId());
        vo.setStrategyKey(telemetry.getStrategyKey());
        vo.setProvider(telemetry.getProvider());
        vo.setModelName(telemetry.getModelName());
        vo.setRole(telemetry.getRole());
        vo.setPromptVersion(telemetry.getPromptVersion());
        vo.setStatus(telemetry.getStatus());
        vo.setLatencyMs(telemetry.getLatencyMs());
        vo.setPromptTokens(telemetry.getPromptTokens());
        vo.setCompletionTokens(telemetry.getCompletionTokens());
        vo.setTotalTokens(telemetry.getTotalTokens());
        vo.setCostMicroCents(telemetry.getCostMicroCents());
        vo.setErrorMessage(telemetry.getErrorMessage());
        vo.setCreatedAt(telemetry.getCreatedAt());
        vo.setUpdatedAt(telemetry.getUpdatedAt());
        return vo;
    }

    private long averageLatency(List<ModelCallTelemetry> records) {
        return Math.round(records.stream()
                .map(ModelCallTelemetry::getLatencyMs)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D));
    }

    private long averageCost(List<ModelCallTelemetry> records) {
        if (records.isEmpty()) {
            return 0L;
        }
        return Math.round(records.stream()
                .mapToInt(item -> defaultZero(item.getCostMicroCents()))
                .average()
                .orElse(0D));
    }

    private long percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0L;
        }
        return Math.round((numerator * 100D) / denominator);
    }

    private boolean isFailed(ModelCallTelemetry item) {
        return "FAILED".equalsIgnoreCase(item.getStatus());
    }

    private boolean isJudgeCall(ModelCallTelemetry item) {
        return "JUDGE".equalsIgnoreCase(item.getRole());
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
