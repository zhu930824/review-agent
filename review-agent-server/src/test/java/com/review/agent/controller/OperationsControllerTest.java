package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationDashboardVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsStrategyPressureVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.dto.OperationsTelemetryReadinessVO;
import com.review.agent.service.OperationsCiHealthActionService;
import com.review.agent.service.OperationsRemediationQueueService;
import com.review.agent.service.OperationsService;
import com.review.agent.service.OperationsStrategyPressureService;
import com.review.agent.service.OperationsTaskService;
import com.review.agent.service.OperationsTelemetryReadinessService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsControllerTest {

    private final FakeOperationsService operationsService = new FakeOperationsService();
    private final FakeStrategyPressureService pressureService = new FakeStrategyPressureService();
    private final FakeTelemetryReadinessService readinessService = new FakeTelemetryReadinessService();
    private final FakeCiHealthActionService ciHealthActionService = new FakeCiHealthActionService();
    private final FakeTaskService taskService = new FakeTaskService();
    private final FakeRemediationQueueService remediationQueueService = new FakeRemediationQueueService();
    private final OperationsController controller = new OperationsController(
            operationsService,
            pressureService,
            readinessService,
            ciHealthActionService,
            taskService,
            remediationQueueService);

    @Test
    void strategyPressureDelegatesToService() {
        Result<List<OperationsStrategyPressureVO>> result = controller.strategyPressure();

        assertTrue(result.isSuccess());
        assertEquals(1, pressureService.calls);
        assertEquals("quality-gate", result.getData().get(0).getStrategyKey());
    }

    @Test
    void remediationQueueDelegatesToService() {
        Result<List<OperationFindingVO>> result = controller.remediationQueue(25);

        assertTrue(result.isSuccess());
        assertEquals(25, remediationQueueService.lastLimit);
        assertEquals(7L, result.getData().get(0).getId());
    }

    @Test
    void ownerLoadDelegatesToService() {
        Result<List<OperationOwnerLoadVO>> result = controller.ownerLoad();

        assertTrue(result.isSuccess());
        assertEquals(1, remediationQueueService.ownerLoadCalls);
        assertEquals("Security Owner", result.getData().get(0).getRole());
    }

    @Test
    void ruleLearningCandidatesDelegateToService() {
        Result<List<OperationRuleLearningCandidateVO>> result = controller.ruleLearningCandidates(8);

        assertTrue(result.isSuccess());
        assertEquals(8, remediationQueueService.lastRuleLearningLimit);
        assertEquals(9L, result.getData().get(0).getFindingId());
        assertEquals("PROMOTE_TO_RULE", result.getData().get(0).getAction());
    }

    @Test
    void businessImpactDelegatesToService() {
        Result<OperationBusinessImpactVO> result = controller.businessImpact();

        assertTrue(result.isSuccess());
        assertEquals(1, remediationQueueService.businessImpactCalls);
        assertEquals(12L, result.getData().getHoursSaved());
        assertEquals(18L, result.getData().getAvoidedReworkHours());
    }

    @Test
    void telemetryReadinessDelegatesToService() {
        Result<List<OperationsTelemetryReadinessVO>> result = controller.telemetryReadiness();

        assertTrue(result.isSuccess());
        assertEquals(1, readinessService.calls);
        assertEquals("quality-gate", result.getData().get(0).getStrategyKey());
        assertEquals("READY", result.getData().get(0).getReadinessLevel());
    }

    @Test
    void ciHealthActionsDelegateToService() {
        Result<List<OperationsCiHealthActionVO>> result = controller.ciHealthActions();

        assertTrue(result.isSuccess());
        assertEquals(1, ciHealthActionService.calls);
        assertEquals("jenkins-pipeline", result.getData().get(0).getConnectorKey());
        assertEquals("CI Owner", result.getData().get(0).getOwnerRole());
    }

    @Test
    void tasksDelegateToService() {
        Result<List<OperationsTaskVO>> result = controller.tasks(15);

        assertTrue(result.isSuccess());
        assertEquals(15, taskService.lastLimit);
        assertEquals("FINDING-7", result.getData().get(0).getTaskKey());
    }

    private static class FakeOperationsService implements OperationsService {
        @Override
        public OperationDashboardVO getDashboard() {
            return new OperationDashboardVO();
        }
    }

    private static class FakeStrategyPressureService implements OperationsStrategyPressureService {
        private int calls;

        @Override
        public List<OperationsStrategyPressureVO> listPressure() {
            calls++;
            OperationsStrategyPressureVO vo = new OperationsStrategyPressureVO();
            vo.setStrategyKey("quality-gate");
            return List.of(vo);
        }
    }

    private static class FakeTelemetryReadinessService implements OperationsTelemetryReadinessService {
        private int calls;

        @Override
        public List<OperationsTelemetryReadinessVO> listReadiness() {
            calls++;
            OperationsTelemetryReadinessVO vo = new OperationsTelemetryReadinessVO();
            vo.setStrategyKey("quality-gate");
            vo.setReadinessLevel("READY");
            return List.of(vo);
        }
    }

    private static class FakeCiHealthActionService implements OperationsCiHealthActionService {
        private int calls;

        @Override
        public List<OperationsCiHealthActionVO> listActions() {
            calls++;
            OperationsCiHealthActionVO vo = new OperationsCiHealthActionVO();
            vo.setConnectorKey("jenkins-pipeline");
            vo.setOwnerRole("CI Owner");
            return List.of(vo);
        }
    }

    private static class FakeTaskService implements OperationsTaskService {
        private int lastLimit;

        @Override
        public List<OperationsTaskVO> listTasks(int limit) {
            lastLimit = limit;
            OperationsTaskVO vo = new OperationsTaskVO();
            vo.setTaskKey("FINDING-7");
            return List.of(vo);
        }
    }

    private static class FakeRemediationQueueService implements OperationsRemediationQueueService {
        private int lastLimit;
        private int lastRuleLearningLimit;
        private int ownerLoadCalls;
        private int businessImpactCalls;

        @Override
        public List<OperationFindingVO> listQueue(int limit) {
            lastLimit = limit;
            OperationFindingVO vo = new OperationFindingVO();
            vo.setId(7L);
            return List.of(vo);
        }

        @Override
        public List<OperationOwnerLoadVO> listOwnerLoad() {
            ownerLoadCalls++;
            OperationOwnerLoadVO vo = new OperationOwnerLoadVO();
            vo.setRole("Security Owner");
            vo.setCount(2L);
            vo.setPercent(100L);
            return List.of(vo);
        }

        @Override
        public List<OperationRuleLearningCandidateVO> listRuleLearningCandidates(int limit) {
            lastRuleLearningLimit = limit;
            OperationRuleLearningCandidateVO vo = new OperationRuleLearningCandidateVO();
            vo.setFindingId(9L);
            vo.setAction("PROMOTE_TO_RULE");
            vo.setRuleTitle("Promote rule: finding-9");
            vo.setReason("Confirmed high-signal finding");
            return List.of(vo);
        }

        @Override
        public OperationBusinessImpactVO estimateBusinessImpact() {
            businessImpactCalls++;
            OperationBusinessImpactVO vo = new OperationBusinessImpactVO();
            vo.setHoursSaved(12L);
            vo.setAvoidedReworkHours(18L);
            vo.setExecutiveSummary("Backend business impact estimate");
            return vo;
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
}
