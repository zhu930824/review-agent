package com.review.agent.service.impl;

import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateFindingInput;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.infrastructure.persistence.PrePrGateRepository;
import com.review.agent.service.PrePrGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrePrGateServiceImpl implements PrePrGateService {

    private static final String PASSED = "PASSED";
    private static final String BLOCKED = "BLOCKED";
    private static final String NEEDS_HUMAN_REVIEW = "NEEDS_HUMAN_REVIEW";
    private static final String RUNNING = "RUNNING";

    private final PrePrGateRepository repository;

    @Override
    public PrePrGateVO getGate(Long reviewId) {
        return repository.findGate(reviewId).orElseGet(() -> refreshGate(reviewId));
    }

    @Override
    public PrePrGateVO initializeGate(Long reviewId) {
        PrePrGateVO gate = repository.findGate(reviewId).orElseGet(() -> refreshGate(reviewId));
        repository.appendGateHistory(reviewId, gate.getGateStatus(), "INITIALIZED", null, "system");
        return gate;
    }

    @Override
    public PrePrGateVO refreshGate(Long reviewId) {
        String reviewStatus = repository.findReviewStatus(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));
        PrePrGateVO existing = repository.findGate(reviewId).orElseGet(PrePrGateVO::new);
        LocalDateTime now = LocalDateTime.now();
        PrePrGateVO gate = calculateGate(reviewId, reviewStatus, repository.listFindings(reviewId));
        gate.setId(existing.getId());
        gate.setCreatedAt(existing.getCreatedAt() == null ? now : existing.getCreatedAt());
        gate.setUpdatedAt(now);
        return repository.upsertGate(gate);
    }

    @Override
    public PrePrGateVO decideGate(Long reviewId, PrePrGateDecisionRequest request) {
        PrePrGateVO existing = repository.findGate(reviewId).orElseGet(() -> refreshGate(reviewId));
        LocalDateTime now = LocalDateTime.now();
        existing.setReviewId(reviewId);
        existing.setGateStatus(normalizeDecisionStatus(request.getGateStatus()));
        existing.setBlockedReasons(List.of("人工决策：" + request.getReason()));
        existing.setDecidedBy(request.getDecidedBy());
        existing.setDecidedAt(now);
        existing.setUpdatedAt(now);
        if (existing.getCreatedAt() == null) {
            existing.setCreatedAt(now);
        }
        PrePrGateVO gate = repository.upsertGate(existing);
        repository.appendGateHistory(reviewId, gate.getGateStatus(), "MANUAL_DECISION", request.getReason(), request.getDecidedBy());
        return gate;
    }

    private PrePrGateVO calculateGate(Long reviewId, String reviewStatus, List<PrePrGateFindingInput> findings) {
        PrePrGateVO gate = new PrePrGateVO();
        gate.setReviewId(reviewId);
        if ("RUNNING".equals(reviewStatus) || "PENDING".equals(reviewStatus)) {
            gate.setGateStatus(RUNNING);
            return gate;
        }

        long blockerCount = findings.stream()
                .filter(finding -> "BLOCKER".equals(finding.getSeverity()))
                .count();
        long pendingMajorCount = findings.stream()
                .filter(finding -> "MAJOR".equals(finding.getSeverity()))
                .filter(finding -> "PENDING".equals(finding.getHumanStatus()))
                .count();
        List<String> reasons = new ArrayList<>();
        if (blockerCount > 0) {
            reasons.add("存在 " + blockerCount + " 个 BLOCKER 级别问题，Pre-PR 暂不可通过。");
        }
        if (pendingMajorCount > 0) {
            reasons.add("存在 " + pendingMajorCount + " 个 MAJOR 问题等待人工复核。");
        }
        gate.setBlockedReasons(reasons);
        if (blockerCount > 0) {
            gate.setGateStatus(BLOCKED);
        } else if (pendingMajorCount > 0) {
            gate.setGateStatus(NEEDS_HUMAN_REVIEW);
        } else {
            gate.setGateStatus(PASSED);
        }
        return gate;
    }

    private String normalizeDecisionStatus(String status) {
        if (PASSED.equals(status) || BLOCKED.equals(status) || NEEDS_HUMAN_REVIEW.equals(status)) {
            return status;
        }
        if ("APPROVED".equals(status)) {
            return PASSED;
        }
        throw new IllegalArgumentException("Unsupported gate status: " + status);
    }
}
