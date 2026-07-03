package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.OperationsRemediationQueueRepository;
import com.review.agent.infrastructure.persistence.OperationsRuleLearningDecisionRepository;
import com.review.agent.infrastructure.persistence.GovernanceRulePackChangeRepository;
import com.review.agent.service.OperationsRemediationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationsRemediationQueueServiceImpl implements OperationsRemediationQueueService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final long AVERAGE_MANUAL_REVIEW_MINUTES = 35L;

    private final OperationsRemediationQueueRepository repository;
    private final OperationsRuleLearningDecisionRepository ruleLearningDecisionRepository;
    private final GovernanceRulePackChangeRepository governanceRulePackChangeRepository;

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
        Set<Long> decidedFindingIds = ruleLearningDecisionRepository.listDecidedFindingIds(MAX_LIMIT);
        return repository.listFindings(safeLimit).stream()
                .map(this::toRuleLearningCandidate)
                .filter(Objects::nonNull)
                .filter(candidate -> !decidedFindingIds.contains(candidate.getFindingId()))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public OperationBusinessImpactVO estimateBusinessImpact() {
        List<OperationFindingVO> findings = repository.listFindings(MAX_LIMIT);
        long monthlyReviews = findings.stream()
                .map(OperationFindingVO::getReviewId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long totalFindings = findings.size();
        long reviewedFindings = findings.stream()
                .filter(finding -> finding.getHumanStatus() == HumanStatus.CONFIRMED || finding.getHumanStatus() == HumanStatus.DISMISSED)
                .count();
        long automationCoveragePercent = totalFindings == 0 ? 0L : Math.round((reviewedFindings * 100D) / totalFindings);
        long blockerFindings = findings.stream().filter(finding -> finding.getSeverity() == Severity.BLOCKER).count();
        long majorFindings = findings.stream().filter(finding -> finding.getSeverity() == Severity.MAJOR).count();
        long hoursSaved = Math.round((monthlyReviews * AVERAGE_MANUAL_REVIEW_MINUTES * automationCoveragePercent) / 100D / 60D);
        long avoidedReworkHours = Math.round(blockerFindings * 6D + majorFindings * 2.5D);

        OperationBusinessImpactVO vo = new OperationBusinessImpactVO();
        vo.setMonthlyReviews(monthlyReviews);
        vo.setAverageManualReviewMinutes(AVERAGE_MANUAL_REVIEW_MINUTES);
        vo.setAutomationCoveragePercent(automationCoveragePercent);
        vo.setBlockerFindings(blockerFindings);
        vo.setMajorFindings(majorFindings);
        vo.setHoursSaved(hoursSaved);
        vo.setAvoidedReworkHours(avoidedReworkHours);
        vo.setExecutiveSummary(businessImpactSummary(monthlyReviews, hoursSaved, avoidedReworkHours));
        return vo;
    }

    @Override
    public void confirmFinding(Long findingId) {
        repository.updateHumanStatus(findingId, HumanStatus.CONFIRMED);
    }

    @Override
    public void dismissFinding(Long findingId) {
        repository.updateHumanStatus(findingId, HumanStatus.DISMISSED);
    }

    @Override
    public void acceptRuleLearningCandidate(Long findingId) {
        OperationRuleLearningCandidateVO candidate = requireRuleLearningCandidate(findingId);
        ruleLearningDecisionRepository.upsertDecision(
                findingId,
                candidate.getAction(),
                "ACCEPTED",
                "operations",
                candidate.getReason());
        governanceRulePackChangeRepository.proposeFromRuleLearningCandidate(candidate);
    }

    @Override
    public void rejectRuleLearningCandidate(Long findingId) {
        OperationRuleLearningCandidateVO candidate = requireRuleLearningCandidate(findingId);
        ruleLearningDecisionRepository.upsertDecision(
                findingId,
                candidate.getAction(),
                "REJECTED",
                "operations",
                "Rejected from operations rule learning review.");
    }

    private OperationRuleLearningCandidateVO requireRuleLearningCandidate(Long findingId) {
        return repository.listFindings(MAX_LIMIT).stream()
                .filter(finding -> Objects.equals(finding.getId(), findingId))
                .map(this::toRuleLearningCandidate)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rule learning candidate not found: " + findingId));
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

    private String businessImpactSummary(long monthlyReviews, long hoursSaved, long avoidedReworkHours) {
        if (monthlyReviews == 0) {
            return "No recent review data yet; business impact will be estimated after findings are generated.";
        }
        return "Based on " + monthlyReviews + " recent reviews, estimated monthly savings are "
                + hoursSaved + " review hours and " + avoidedReworkHours + " avoided rework hours.";
    }
}
