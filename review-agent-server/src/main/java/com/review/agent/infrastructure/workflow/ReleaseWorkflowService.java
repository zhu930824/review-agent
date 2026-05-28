package com.review.agent.infrastructure.workflow;

import com.review.agent.domain.entity.PrePrGate;
import com.review.agent.infrastructure.persistence.PrePrGateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final PrePrGateMapper prePrGateMapper;

    public String startReleaseWorkflow(Long reviewId) {
        PrePrGate gate = prePrGateMapper.selectOne(
                new LambdaQueryWrapper<PrePrGate>().eq(PrePrGate::getReviewId, reviewId));

        if (gate == null || !"PASSED".equals(gate.getGateStatus())) {
            log.warn("Pre-PR 门禁未通过，无法启动发布流程: reviewId={}", reviewId);
            return null;
        }

        return workflowEngine.startWorkflow("release",
                Map.of("reviewId", String.valueOf(reviewId), "gateId", String.valueOf(gate.getId())));
    }

    public List<WorkflowStep> getSteps() {
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(WorkflowStep.builder()
                .stepKey("pre-pr-gate")
                .name("Pre-PR 审查门禁")
                .type("GATE")
                .responsible("SYSTEM")
                .order(1)
                .status("REQUIRED")
                .build());
        steps.add(WorkflowStep.builder()
                .stepKey("human-review")
                .name("人工确认")
                .type("MANUAL")
                .responsible("HUMAN")
                .order(2)
                .dependsOn("pre-pr-gate")
                .status("REQUIRED")
                .build());
        steps.add(WorkflowStep.builder()
                .stepKey("merge-validate")
                .name("合并校验")
                .type("GATE")
                .responsible("SYSTEM")
                .order(3)
                .dependsOn("human-review")
                .status("REQUIRED")
                .build());
        steps.add(WorkflowStep.builder()
                .stepKey("deploy-staging")
                .name("部署预发环境")
                .type("DEPLOY")
                .responsible("CI")
                .order(4)
                .dependsOn("merge-validate")
                .status("REQUIRED")
                .build());
        steps.add(WorkflowStep.builder()
                .stepKey("smoke-test")
                .name("冒烟测试")
                .type("TEST")
                .responsible("CI")
                .order(5)
                .dependsOn("deploy-staging")
                .status("REQUIRED")
                .build());
        steps.add(WorkflowStep.builder()
                .stepKey("release-prod")
                .name("发布生产环境")
                .type("DEPLOY")
                .responsible("HUMAN")
                .order(6)
                .dependsOn("smoke-test")
                .status("REQUIRED")
                .build());
        return steps;
    }
}
