package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.OperationsRemediationQueueRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsRemediationQueueServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final OperationsRemediationQueueServiceImpl service = new OperationsRemediationQueueServiceImpl(repository);

    @Test
    void listsOpenFindingsByOperationalPriority() {
        repository.findings.add(finding(1L, Severity.MINOR, HumanStatus.PENDING, false));
        repository.findings.add(finding(2L, Severity.BLOCKER, HumanStatus.PENDING, true));
        repository.findings.add(finding(3L, Severity.MAJOR, HumanStatus.CONFIRMED, true));
        repository.findings.add(finding(4L, Severity.MAJOR, HumanStatus.DISMISSED, true));

        List<OperationFindingVO> result = service.listQueue(2);

        assertEquals(List.of(2L, 3L), result.stream().map(OperationFindingVO::getId).toList());
        assertEquals(1, repository.calls);
    }

    @Test
    void normalizesInvalidLimit() {
        repository.findings.add(finding(1L, Severity.BLOCKER, HumanStatus.PENDING, false));

        List<OperationFindingVO> result = service.listQueue(0);

        assertEquals(1, result.size());
        assertEquals(50, repository.lastLimit);
    }

    @Test
    void summarizesOwnerLoadFromOpenFindings() {
        repository.findings.add(finding(1L, FindingCategory.SECURITY, HumanStatus.PENDING));
        repository.findings.add(finding(2L, FindingCategory.SECURITY, HumanStatus.CONFIRMED));
        repository.findings.add(finding(3L, FindingCategory.PERFORMANCE, HumanStatus.PENDING));
        repository.findings.add(finding(4L, FindingCategory.CODE_STYLE, HumanStatus.DISMISSED));

        List<OperationOwnerLoadVO> result = service.listOwnerLoad();

        assertEquals("Security Owner", result.get(0).getRole());
        assertEquals(2L, result.get(0).getCount());
        assertEquals(67L, result.get(0).getPercent());
        assertEquals("Performance Owner", result.get(1).getRole());
        assertEquals(1L, result.get(1).getCount());
        assertEquals(33L, result.get(1).getPercent());
        assertEquals(100, repository.lastLimit);
    }

    @Test
    void listsRuleLearningCandidatesFromReviewedFindings() {
        OperationFindingVO crossHitConfirmed = finding(1L, Severity.MAJOR, HumanStatus.CONFIRMED, true);
        crossHitConfirmed.setConfidence(new BigDecimal("0.72"));
        OperationFindingVO highConfidenceConfirmed = finding(2L, Severity.MINOR, HumanStatus.CONFIRMED, false);
        highConfidenceConfirmed.setConfidence(new BigDecimal("0.91"));
        OperationFindingVO lowConfidenceDismissed = finding(3L, Severity.MINOR, HumanStatus.DISMISSED, false);
        lowConfidenceDismissed.setConfidence(new BigDecimal("0.40"));
        OperationFindingVO pending = finding(4L, Severity.MINOR, HumanStatus.PENDING, true);
        pending.setConfidence(new BigDecimal("0.95"));
        repository.findings.add(crossHitConfirmed);
        repository.findings.add(highConfidenceConfirmed);
        repository.findings.add(lowConfidenceDismissed);
        repository.findings.add(pending);

        List<OperationRuleLearningCandidateVO> result = service.listRuleLearningCandidates(10);

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(OperationRuleLearningCandidateVO::getFindingId).toList());
        assertEquals(List.of("PROMOTE_TO_RULE", "PROMOTE_TO_RULE", "SUPPRESS_PATTERN"), result.stream().map(OperationRuleLearningCandidateVO::getAction).toList());
        assertEquals(10, repository.lastLimit);
        assertEquals(1, repository.allFindingCalls);
    }

    private OperationFindingVO finding(Long id, Severity severity, HumanStatus humanStatus, boolean crossHit) {
        OperationFindingVO vo = new OperationFindingVO();
        vo.setId(id);
        vo.setReviewId(100L + id);
        vo.setProjectName("project-" + id);
        vo.setTitle("finding-" + id);
        vo.setSeverity(severity);
        vo.setCategory(FindingCategory.SECURITY);
        vo.setHumanStatus(humanStatus);
        vo.setIsCrossHit(crossHit);
        return vo;
    }

    private OperationFindingVO finding(Long id, FindingCategory category, HumanStatus humanStatus) {
        OperationFindingVO vo = finding(id, Severity.MAJOR, humanStatus, false);
        vo.setCategory(category);
        return vo;
    }

    private static class FakeRepository implements OperationsRemediationQueueRepository {
        private final List<OperationFindingVO> findings = new ArrayList<>();
        private int calls;
        private int allFindingCalls;
        private int lastLimit;

        @Override
        public List<OperationFindingVO> listOpenFindings(int limit) {
            calls++;
            lastLimit = limit;
            return findings;
        }

        @Override
        public List<OperationFindingVO> listFindings(int limit) {
            allFindingCalls++;
            lastLimit = limit;
            return findings;
        }
    }
}
