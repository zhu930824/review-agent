package com.review.agent.service.impl;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.domain.entity.ModelCallTelemetry;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.infrastructure.persistence.ModelTelemetryRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelTelemetryServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final ModelTelemetryServiceImpl service = new ModelTelemetryServiceImpl(repository);

    @Test
    void recordPersistsModelCallTelemetry() {
        ModelCallTelemetryRequest request = request("quality-gate", "DashScope", "qwen-max", "SUCCESS", 1200, 900, 320);

        ModelCallTelemetryVO result = service.record(request);

        assertEquals("DashScope", repository.records.get(0).getProvider());
        assertEquals("qwen-max", result.getModelName());
        assertEquals(1220, result.getTotalTokens());
    }

    @Test
    void summaryAggregatesCostLatencyAndStrategyEffect() {
        repository.records.add(entity(101L, "quality-gate", "DashScope", "qwen-max", "SUCCESS", 1000, 500, 200, 60));
        repository.records.add(entity(101L, "quality-gate", "DashScope", "qwen-max", "FAILED", 300, 100, 0, 30));
        repository.records.add(entity(202L, "cross-check", "OpenAI", "gpt-4.1", "SUCCESS", 700, 400, 120, 40));
        repository.findings.add(finding(101L, HumanStatus.CONFIRMED));
        repository.findings.add(finding(101L, HumanStatus.DISMISSED));
        repository.findings.add(finding(101L, HumanStatus.PENDING));
        repository.findings.add(finding(202L, HumanStatus.CONFIRMED));

        ModelTelemetrySummaryVO result = service.summary();

        assertEquals(3L, result.getTotalCalls());
        assertEquals(1L, result.getFailedCalls());
        assertEquals(1320L, result.getTotalTokens());
        assertEquals(130L, result.getTotalCostMicroCents());
        assertEquals(667L, result.getAvgLatencyMs());
        assertEquals(33L, result.getFailureRatePercent());
        assertEquals(43L, result.getAvgCostMicroCents());
        assertEquals(2, result.getStrategies().size());
        assertEquals("quality-gate", result.getStrategies().get(0).getStrategyKey());
        assertEquals(2L, result.getStrategies().get(0).getTotalCalls());
        assertEquals(1L, result.getStrategies().get(0).getFailedCalls());
        assertEquals(50L, result.getStrategies().get(0).getFailureRatePercent());
        assertEquals(45L, result.getStrategies().get(0).getAvgCostMicroCents());
        assertEquals(1L, result.getStrategies().get(0).getConfirmedFindings());
        assertEquals(1L, result.getStrategies().get(0).getDismissedFindings());
        assertEquals(1L, result.getStrategies().get(0).getPendingFindings());
        assertEquals(33L, result.getStrategies().get(0).getConfirmationRatePercent());
        assertEquals(50L, result.getStrategies().get(0).getFalsePositiveProxyPercent());
    }

    @Test
    void summaryAddsStrategyDiagnosticsForHitRateCrossModelAndJudgeHealth() {
        repository.records.add(entity(301L, "architecture-board", "DashScope", "qwen-plus", "WORKER", "SUCCESS", 900, 300, 100, 20));
        repository.records.add(entity(301L, "architecture-board", "DashScope", "qwen-max", "JUDGE", "FAILED", 1100, 200, 0, 40));
        repository.records.add(entity(302L, "architecture-board", "OpenAI", "deepseek-v3", "WORKER", "SUCCESS", 700, 220, 80, 30));
        repository.records.add(entity(303L, "architecture-board", "OpenAI", "kimi-k2", "WORKER", "SUCCESS", 650, 180, 70, 25));
        repository.findings.add(finding(301L, HumanStatus.CONFIRMED, true));
        repository.findings.add(finding(302L, HumanStatus.DISMISSED, false));

        ModelTelemetrySummaryVO.StrategyTelemetryVO strategy = service.summary().getStrategies().get(0);

        assertEquals(3L, strategy.getReviewedReviews());
        assertEquals(2L, strategy.getTotalFindings());
        assertEquals(67L, strategy.getStrategyHitRatePercent());
        assertEquals(1L, strategy.getFindingsPerReview());
        assertEquals(1L, strategy.getCrossHitFindings());
        assertEquals(50L, strategy.getCrossHitRatePercent());
        assertEquals(4L, strategy.getModelDiversity());
        assertEquals(1L, strategy.getJudgeCalls());
        assertEquals(100L, strategy.getJudgeFailureRatePercent());
    }

    private ModelCallTelemetryRequest request(String strategyKey, String provider, String modelName, String status,
            Integer latencyMs, Integer promptTokens, Integer completionTokens) {
        ModelCallTelemetryRequest request = new ModelCallTelemetryRequest();
        request.setStrategyKey(strategyKey);
        request.setProvider(provider);
        request.setModelName(modelName);
        request.setStatus(status);
        request.setLatencyMs(latencyMs);
        request.setPromptTokens(promptTokens);
        request.setCompletionTokens(completionTokens);
        return request;
    }

    private ModelCallTelemetry entity(Long reviewId, String strategyKey, String provider, String modelName, String status,
            Integer latencyMs, Integer promptTokens, Integer completionTokens, Integer costMicroCents) {
        return entity(reviewId, strategyKey, provider, modelName, null, status, latencyMs, promptTokens, completionTokens, costMicroCents);
    }

    private ModelCallTelemetry entity(Long reviewId, String strategyKey, String provider, String modelName, String role, String status,
            Integer latencyMs, Integer promptTokens, Integer completionTokens, Integer costMicroCents) {
        ModelCallTelemetry entity = new ModelCallTelemetry();
        entity.setReviewId(reviewId);
        entity.setStrategyKey(strategyKey);
        entity.setProvider(provider);
        entity.setModelName(modelName);
        entity.setRole(role);
        entity.setStatus(status);
        entity.setLatencyMs(latencyMs);
        entity.setPromptTokens(promptTokens);
        entity.setCompletionTokens(completionTokens);
        entity.setTotalTokens(promptTokens + completionTokens);
        entity.setCostMicroCents(costMicroCents);
        return entity;
    }

    private ReviewFinding finding(Long reviewId, HumanStatus humanStatus) {
        return finding(reviewId, humanStatus, false);
    }

    private ReviewFinding finding(Long reviewId, HumanStatus humanStatus, Boolean crossHit) {
        ReviewFinding finding = new ReviewFinding();
        finding.setReviewId(reviewId);
        finding.setHumanStatus(humanStatus);
        finding.setIsCrossHit(crossHit);
        return finding;
    }

    private static class FakeRepository implements ModelTelemetryRepository {
        private final List<ModelCallTelemetry> records = new ArrayList<>();
        private final List<ReviewFinding> findings = new ArrayList<>();

        @Override
        public ModelCallTelemetry insert(ModelCallTelemetry telemetry) {
            records.add(telemetry);
            telemetry.setId((long) records.size());
            return telemetry;
        }

        @Override
        public List<ModelCallTelemetry> listRecent(int limit) {
            return records;
        }

        @Override
        public List<ReviewFinding> listFindingsByReviewIds(List<Long> reviewIds) {
            return findings.stream()
                    .filter(finding -> reviewIds.contains(finding.getReviewId()))
                    .toList();
        }
    }
}
