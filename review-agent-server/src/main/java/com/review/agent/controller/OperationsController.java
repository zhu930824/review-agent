package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.BatchUpdateOperationsTaskRequest;
import com.review.agent.domain.dto.CloseOperationsTaskRequest;
import com.review.agent.domain.dto.LinkOperationsExternalIssueRequest;
import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationDashboardVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsExternalIssueVO;
import com.review.agent.domain.dto.OperationsStrategyPressureVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.dto.OperationsTelemetryReadinessVO;
import com.review.agent.domain.dto.UpdateOperationsTaskRequest;
import com.review.agent.service.OperationsCiHealthActionService;
import com.review.agent.service.OperationsRemediationQueueService;
import com.review.agent.service.OperationsService;
import com.review.agent.service.OperationsStrategyPressureService;
import com.review.agent.service.OperationsTaskService;
import com.review.agent.service.OperationsTelemetryReadinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/ci-health-actions/{actionKey}/notify")
    public Result<IntegrationActionLogVO> notifyCiHealthAction(@PathVariable("actionKey") String actionKey) {
        return Result.success(ciHealthActionService.notifyAction(actionKey));
    }

    @GetMapping("/tasks")
    public Result<List<OperationsTaskVO>> tasks(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(taskService.listTasks(limit));
    }

    @GetMapping("/tasks/sla-alerts")
    public Result<List<OperationsTaskVO>> taskSlaAlerts(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(taskService.listSlaAlerts(limit));
    }

    @PostMapping("/tasks/sync")
    public Result<List<OperationsTaskVO>> syncTasks(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(taskService.syncTasks(limit));
    }

    @PatchMapping("/tasks/batch")
    public Result<Void> batchUpdateTasks(@RequestBody BatchUpdateOperationsTaskRequest request) {
        taskService.updateTasks(
                request.getTaskKeys(),
                request.getStatus(),
                request.getOwnerRole(),
                request.getSlaHours());
        return Result.success();
    }

    @PatchMapping("/tasks/{taskKey}")
    public Result<Void> updateTask(
            @PathVariable("taskKey") String taskKey,
            @RequestBody(required = false) UpdateOperationsTaskRequest request) {
        taskService.updateTask(
                taskKey,
                request == null ? null : request.getStatus(),
                request == null ? null : request.getOwnerRole(),
                request == null ? null : request.getSlaHours());
        return Result.success();
    }

    @PostMapping("/tasks/{taskKey}/gitlab-issue")
    public Result<OperationsExternalIssueVO> syncTaskToGitLabIssue(@PathVariable("taskKey") String taskKey) {
        return Result.success(taskService.syncGitLabIssue(taskKey));
    }

    @PostMapping("/tasks/{taskKey}/gitlab-issue/refresh")
    public Result<OperationsExternalIssueVO> refreshTaskGitLabIssue(@PathVariable("taskKey") String taskKey) {
        return Result.success(taskService.refreshGitLabIssue(taskKey));
    }

    @PostMapping("/tasks/{taskKey}/external-issue")
    public Result<OperationsExternalIssueVO> linkTaskExternalIssue(
            @PathVariable("taskKey") String taskKey,
            @RequestBody(required = false) LinkOperationsExternalIssueRequest request) {
        return Result.success(taskService.linkExternalIssue(taskKey, request));
    }

    @PostMapping("/tasks/{taskKey}/close")
    public Result<Void> closeTask(
            @PathVariable("taskKey") String taskKey,
            @RequestBody(required = false) CloseOperationsTaskRequest request) {
        taskService.closeTask(taskKey, request == null ? null : request.getCloseReason());
        return Result.success();
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
