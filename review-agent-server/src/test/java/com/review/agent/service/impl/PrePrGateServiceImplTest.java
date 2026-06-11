package com.review.agent.service.impl;

import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateFindingInput;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.infrastructure.persistence.PrePrGateRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrePrGateServiceImplTest {

    private final FakePrePrGateRepository repository = new FakePrePrGateRepository();
    private final PrePrGateServiceImpl service = new PrePrGateServiceImpl(repository);

    @Test
    void returnsRunningWhenReviewIsStillExecuting() {
        repository.reviewStatus = "RUNNING";

        PrePrGateVO gate = service.refreshGate(42L);

        assertEquals("RUNNING", gate.getGateStatus());
        assertTrue(gate.getBlockedReasons().isEmpty());
    }

    @Test
    void blocksWhenReviewHasBlockerFinding() {
        repository.reviewStatus = "COMPLETED";
        repository.findings.add(new PrePrGateFindingInput("BLOCKER", "PENDING", "SQL 注入风险"));

        PrePrGateVO gate = service.refreshGate(42L);

        assertEquals("BLOCKED", gate.getGateStatus());
        assertEquals(List.of("存在 1 个 BLOCKER 级别问题，Pre-PR 暂不可通过。"), gate.getBlockedReasons());
    }

    @Test
    void requiresHumanReviewWhenMajorFindingIsPending() {
        repository.reviewStatus = "COMPLETED";
        repository.findings.add(new PrePrGateFindingInput("MAJOR", "PENDING", "异常处理缺失"));

        PrePrGateVO gate = service.refreshGate(42L);

        assertEquals("NEEDS_HUMAN_REVIEW", gate.getGateStatus());
        assertEquals(List.of("存在 1 个 MAJOR 问题等待人工复核。"), gate.getBlockedReasons());
    }

    @Test
    void passesWhenSeriousFindingsHaveBeenHandled() {
        repository.reviewStatus = "COMPLETED";
        repository.findings.add(new PrePrGateFindingInput("MAJOR", "CONFIRMED", "需要补测试"));
        repository.findings.add(new PrePrGateFindingInput("MINOR", "PENDING", "命名可读性"));

        PrePrGateVO gate = service.refreshGate(42L);

        assertEquals("PASSED", gate.getGateStatus());
        assertTrue(gate.getBlockedReasons().isEmpty());
    }

    @Test
    void persistsManualDecisionWithAuditFields() {
        PrePrGateDecisionRequest request = new PrePrGateDecisionRequest();
        request.setGateStatus("PASSED");
        request.setReason("负责人确认可合并");
        request.setDecidedBy("alice");

        PrePrGateVO gate = service.decideGate(42L, request);

        assertEquals("PASSED", gate.getGateStatus());
        assertEquals("alice", gate.getDecidedBy());
        assertEquals(List.of("人工决策：负责人确认可合并"), gate.getBlockedReasons());
    }

    @Test
    void initializesGateForNewPrePrReviewAndRecordsHistory() {
        repository.reviewStatus = "PENDING";

        PrePrGateVO gate = service.initializeGate(42L);

        assertEquals("RUNNING", gate.getGateStatus());
        assertEquals(1, repository.history.size());
        assertEquals("INITIALIZED:RUNNING:system", repository.history.get(0));
    }

    @Test
    void manualDecisionAppendsDecisionHistory() {
        PrePrGateDecisionRequest request = new PrePrGateDecisionRequest();
        request.setGateStatus("BLOCKED");
        request.setReason("负责人要求补测试");
        request.setDecidedBy("alice");

        service.decideGate(42L, request);

        assertEquals(1, repository.history.size());
        assertEquals("MANUAL_DECISION:BLOCKED:alice:负责人要求补测试", repository.history.get(0));
    }

    private static class FakePrePrGateRepository implements PrePrGateRepository {
        private String reviewStatus = "COMPLETED";
        private final List<PrePrGateFindingInput> findings = new ArrayList<>();
        private final List<String> history = new ArrayList<>();
        private PrePrGateVO storedGate;

        @Override
        public Optional<String> findReviewStatus(Long reviewId) {
            return Optional.ofNullable(reviewStatus);
        }

        @Override
        public List<PrePrGateFindingInput> listFindings(Long reviewId) {
            return findings;
        }

        @Override
        public Optional<PrePrGateVO> findGate(Long reviewId) {
            return Optional.ofNullable(storedGate);
        }

        @Override
        public PrePrGateVO upsertGate(PrePrGateVO gate) {
            storedGate = gate;
            return storedGate;
        }

        @Override
        public void appendGateHistory(Long reviewId, String gateStatus, String eventType, String reason, String operator) {
            history.add(eventType + ":" + gateStatus + ":" + operator + (reason == null ? "" : ":" + reason));
        }
    }
}
