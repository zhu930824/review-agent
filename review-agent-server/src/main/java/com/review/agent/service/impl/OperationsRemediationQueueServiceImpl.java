package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.OperationsRemediationQueueRepository;
import com.review.agent.service.OperationsRemediationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationsRemediationQueueServiceImpl implements OperationsRemediationQueueService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final OperationsRemediationQueueRepository repository;

    @Override
    public List<OperationFindingVO> listQueue(int limit) {
        int safeLimit = normalizeLimit(limit);
        return repository.listOpenFindings(safeLimit).stream()
                .filter(finding -> finding.getHumanStatus() != HumanStatus.DISMISSED)
                .sorted(Comparator
                        .comparingInt(this::priorityScore).reversed()
                        .thenComparing(OperationFindingVO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public List<OperationOwnerLoadVO> listOwnerLoad() {
        List<OperationFindingVO> openFindings = repository.listOpenFindings(MAX_LIMIT).stream()
                .filter(finding -> finding.getHumanStatus() != HumanStatus.DISMISSED)
                .toList();
        long total = openFindings.size();
        if (total == 0) {
            return List.of();
        }

        Map<String, Long> byOwner = openFindings.stream()
                .collect(Collectors.groupingBy(finding -> ownerRole(finding.getCategory()), Collectors.counting()));

        return byOwner.entrySet().stream()
                .map(entry -> ownerLoad(entry.getKey(), entry.getValue(), total))
                .sorted(Comparator
                        .comparing(OperationOwnerLoadVO::getCount).reversed()
                        .thenComparing(OperationOwnerLoadVO::getRole))
                .toList();
    }

    @Override
    public List<OperationRuleLearningCandidateVO> listRuleLearningCandidates(int limit) {
        int safeLimit = normalizeLimit(limit);
        return repository.listFindings(safeLimit).stream()
                .map(this::toRuleLearningCandidate)
                .filter(Objects::nonNull)
                .limit(safeLimit)
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int priorityScore(OperationFindingVO finding) {
        int score = severityScore(finding.getSeverity());
        if (finding.getHumanStatus() == HumanStatus.PENDING) {
            score += 12;
        }
        if (Boolean.TRUE.equals(finding.getIsCrossHit())) {
            score += 8;
        }
        return score;
    }

    private int severityScore(Severity severity) {
        if (severity == Severity.BLOCKER) {
            return 100;
        }
        if (severity == Severity.MAJOR) {
            return 70;
        }
        if (severity == Severity.MINOR) {
            return 30;
        }
        return 10;
    }

    private String ownerRole(FindingCategory category) {
        if (category == FindingCategory.SECURITY) {
            return "Security Owner";
        }
        if (category == FindingCategory.PERFORMANCE) {
            return "Performance Owner";
        }
        if (category == FindingCategory.BUG || category == FindingCategory.EXCEPTION_HANDLING) {
            return "Tech Lead";
        }
        return "Code Owner";
    }

    private OperationOwnerLoadVO ownerLoad(String role, long count, long total) {
        OperationOwnerLoadVO vo = new OperationOwnerLoadVO();
        vo.setRole(role);
        vo.setCount(count);
        vo.setPercent(Math.round((count * 100D) / total));
        return vo;
    }

    private OperationRuleLearningCandidateVO toRuleLearningCandidate(OperationFindingVO finding) {
        if (finding.getHumanStatus() == HumanStatus.CONFIRMED
                && (Boolean.TRUE.equals(finding.getIsCrossHit()) || confidenceAtLeast(finding, new BigDecimal("0.85")))) {
            return ruleLearningCandidate(
                    finding,
                    "PROMOTE_TO_RULE",
                    "Promote rule: " + finding.getTitle(),
                    "Confirmed high-signal finding can be promoted into a durable review rule.");
        }
        if (finding.getHumanStatus() == HumanStatus.DISMISSED && confidenceAtMost(finding, new BigDecimal("0.50"))) {
            return ruleLearningCandidate(
                    finding,
                    "SUPPRESS_PATTERN",
                    "Suppress pattern: " + finding.getTitle(),
                    "Dismissed low-confidence finding can be added to suppression examples.");
        }
        return null;
    }

    private boolean confidenceAtLeast(OperationFindingVO finding, BigDecimal threshold) {
        BigDecimal confidence = finding.getConfidence() == null ? BigDecimal.ZERO : finding.getConfidence();
        return confidence.compareTo(threshold) >= 0;
    }

    private boolean confidenceAtMost(OperationFindingVO finding, BigDecimal threshold) {
        BigDecimal confidence = finding.getConfidence() == null ? BigDecimal.ONE : finding.getConfidence();
        return confidence.compareTo(threshold) <= 0;
    }

    private OperationRuleLearningCandidateVO ruleLearningCandidate(OperationFindingVO finding, String action, String ruleTitle, String reason) {
        OperationRuleLearningCandidateVO vo = new OperationRuleLearningCandidateVO();
        vo.setFindingId(finding.getId());
        vo.setAction(action);
        vo.setRuleTitle(ruleTitle);
        vo.setReason(reason);
        return vo;
    }
}
