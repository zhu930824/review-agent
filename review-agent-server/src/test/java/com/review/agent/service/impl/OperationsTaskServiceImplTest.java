package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.service.OperationsCiHealthActionService;
import com.review.agent.service.OperationsRemediationQueueService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsTaskServiceImplTest {

    private final FakeRemediationQueueService remediationQueueService = new FakeRemediationQueueService();
    private final FakeCiHealthActionService ciHealthActionService = new FakeCiHealthActionService();
    private final OperationsTaskServiceImpl service = new OperationsTaskServiceImpl(
            remediationQueueService,
            ciHealthActionService);

    @Test
    void mergesFindingAndCiHealthActionsIntoUnifiedTasks() {
        remediationQueueService.findings = List.of(
                finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true),
                finding(8L, 4L, "Minor cleanup", Severity.MINOR, FindingCategory.CODE_STYLE, HumanStatus.PENDING, false));
        ciHealthActionService.actions = List.of(ciAction("jenkins-pipeline", "JENKINS", "CRITICAL"));

        List<OperationsTaskVO> tasks = service.listTasks(10);

        assertEquals(3, tasks.size());
        assertEquals("FINDING-7", tasks.get(0).getTaskKey());
        assertEquals("FINDING", tasks.get(0).getSourceType());
        assertEquals("Security Owner", tasks.get(0).getOwnerRole());
        assertEquals(4L, tasks.get(0).getSlaHours());
        assertEquals("CI_HEALTH", tasks.get(1).getSourceType());
        assertEquals("CI Owner", tasks.get(1).getOwnerRole());
        assertEquals("CI-jenkins-pipeline-UNHEALTHY", tasks.get(1).getTaskKey());
    }

    @Test
    void normalizesLimitForUnifiedTasks() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));

        List<OperationsTaskVO> tasks = service.listTasks(0);

        assertEquals(50, remediationQueueService.lastLimit);
        assertEquals(1, tasks.size());
    }

    private OperationFindingVO finding(
            Long id,
            Long reviewId,
            String title,
            Severity severity,
            FindingCategory category,
            HumanStatus humanStatus,
            boolean crossHit) {
        OperationFindingVO vo = new OperationFindingVO();
        vo.setId(id);
        vo.setReviewId(reviewId);
        vo.setProjectName("review-agent");
        vo.setTitle(title);
        vo.setSeverity(severity);
        vo.setCategory(category);
        vo.setHumanStatus(humanStatus);
        vo.setIsCrossHit(crossHit);
        return vo;
    }

    private OperationsCiHealthActionVO ciAction(String connectorKey, String provider, String severity) {
        OperationsCiHealthActionVO vo = new OperationsCiHealthActionVO();
        vo.setKey(connectorKey + "-UNHEALTHY");
        vo.setConnectorKey(connectorKey);
        vo.setProvider(provider);
        vo.setHealthStatus("UNHEALTHY");
        vo.setSeverity(severity);
        vo.setOwnerRole("CI Owner");
        vo.setSlaHours(4L);
        vo.setLatestSignal("FAILED");
        vo.setRecommendation("Check Jenkins credentials.");
        return vo;
    }

    private static class FakeRemediationQueueService implements OperationsRemediationQueueService {
        private int lastLimit;
        private List<OperationFindingVO> findings = List.of();

        @Override
        public List<OperationFindingVO> listQueue(int limit) {
            lastLimit = limit;
            return findings;
        }

        @Override
        public List<OperationOwnerLoadVO> listOwnerLoad() {
            return List.of();
        }

        @Override
        public List<OperationRuleLearningCandidateVO> listRuleLearningCandidates(int limit) {
            return List.of();
        }

        @Override
        public OperationBusinessImpactVO estimateBusinessImpact() {
            return new OperationBusinessImpactVO();
        }

        @Override
        public void confirmFinding(Long findingId) {
        }

        @Override
        public void dismissFinding(Long findingId) {
        }

        @Override
        public void acceptRuleLearningCandidate(Long findingId) {
        }

        @Override
        public void rejectRuleLearningCandidate(Long findingId) {
        }
    }

    private static class FakeCiHealthActionService implements OperationsCiHealthActionService {
        private List<OperationsCiHealthActionVO> actions = List.of();

        @Override
        public List<OperationsCiHealthActionVO> listActions() {
            return actions;
        }
    }
}
