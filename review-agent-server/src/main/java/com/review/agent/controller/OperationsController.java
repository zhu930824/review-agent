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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;
    private final OperationsStrategyPressureService strategyPressureService;
    private final OperationsTelemetryReadinessService telemetryReadinessService;
    private final OperationsCiHealthActionService ciHealthActionService;
    private final OperationsTaskService taskService;
    private final OperationsRemediationQueueService remediationQueueService;

    @GetMapping("/dashboard")
    public Result<OperationDashboardVO> getDashboard() {
        return Result.success(operationsService.getDashboard());
    }

    @GetMapping("/strategy-pressure")
    public Result<List<OperationsStrategyPressureVO>> strategyPressure() {
        return Result.success(strategyPressureService.listPressure());
    }

    @GetMapping("/telemetry-readiness")
    public Result<List<OperationsTelemetryReadinessVO>> telemetryReadiness() {
        return Result.success(telemetryReadinessService.listReadiness());
    }

    @GetMapping("/ci-health-actions")
    public Result<List<OperationsCiHealthActionVO>> ciHealthActions() {
        return Result.success(ciHealthActionService.listActions());
    }

    @GetMapping("/tasks")
    public Result<List<OperationsTaskVO>> tasks(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(taskService.listTasks(limit));
    }

    @GetMapping("/remediation-queue")
    public Result<List<OperationFindingVO>> remediationQueue(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(remediationQueueService.listQueue(limit));
    }

    @PostMapping("/remediation-queue/{findingId}/confirm")
    public Result<Void> confirmFinding(@PathVariable("findingId") Long findingId) {
        remediationQueueService.confirmFinding(findingId);
        return Result.success();
    }

    @PostMapping("/remediation-queue/{findingId}/dismiss")
    public Result<Void> dismissFinding(@PathVariable("findingId") Long findingId) {
        remediationQueueService.dismissFinding(findingId);
        return Result.success();
    }

    @GetMapping("/owner-load")
    public Result<List<OperationOwnerLoadVO>> ownerLoad() {
        return Result.success(remediationQueueService.listOwnerLoad());
    }

    @GetMapping("/rule-learning-candidates")
    public Result<List<OperationRuleLearningCandidateVO>> ruleLearningCandidates(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(remediationQueueService.listRuleLearningCandidates(limit));
    }

    @PostMapping("/rule-learning-candidates/{findingId}/accept")
    public Result<Void> acceptRuleLearningCandidate(@PathVariable("findingId") Long findingId) {
        remediationQueueService.acceptRuleLearningCandidate(findingId);
        return Result.success();
    }

    @PostMapping("/rule-learning-candidates/{findingId}/reject")
    public Result<Void> rejectRuleLearningCandidate(@PathVariable("findingId") Long findingId) {
        remediationQueueService.rejectRuleLearningCandidate(findingId);
        return Result.success();
    }

    @GetMapping("/business-impact")
    public Result<OperationBusinessImpactVO> businessImpact() {
        return Result.success(remediationQueueService.estimateBusinessImpact());
    }
}
