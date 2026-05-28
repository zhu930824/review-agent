package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.integration.IssueSyncService;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.risk.ChangeRiskPredictor;
import com.review.agent.infrastructure.risk.RiskAssessment;
import com.review.agent.infrastructure.workflow.ReleaseWorkflowService;
import com.review.agent.infrastructure.workflow.WorkflowStep;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final IssueSyncService issueSyncService;
    private final ChangeRiskPredictor riskPredictor;
    private final ReleaseWorkflowService releaseWorkflowService;
    private final ReviewFindingMapper reviewFindingMapper;

    @GetMapping("/sync/sources")
    public Result<List<String>> listSyncSources() {
        return Result.success(issueSyncService.listSources());
    }

    @PostMapping("/sync/{source}/{findingId}")
    public Result<String> syncFinding(
            @PathVariable("source") String source,
            @PathVariable("findingId") Long findingId) {
        ReviewFinding finding = reviewFindingMapper.selectById(findingId);
        if (finding == null) {
            return Result.fail(404, "发现项不存在: " + findingId);
        }
        String issueId = issueSyncService.sync(source, finding);
        if (issueId == null) {
            return Result.fail(400, "不支持的工单来源: " + source);
        }
        return Result.success(issueId);
    }

    @PostMapping("/release/{reviewId}")
    public Result<String> startRelease(@PathVariable("reviewId") Long reviewId) {
        String workflowId = releaseWorkflowService.startReleaseWorkflow(reviewId);
        if (workflowId == null) {
            return Result.fail(400, "Pre-PR 门禁未通过，无法启动发布流程");
        }
        return Result.success(workflowId);
    }

    @GetMapping("/release/steps")
    public Result<List<WorkflowStep>> getReleaseSteps() {
        return Result.success(releaseWorkflowService.getSteps());
    }
}
