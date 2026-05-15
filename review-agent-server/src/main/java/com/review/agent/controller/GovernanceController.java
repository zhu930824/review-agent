package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GovernanceCapabilityVO;
import com.review.agent.domain.dto.GovernanceRulePackVO;
import com.review.agent.domain.dto.IntegrationConnectorVO;
import com.review.agent.domain.dto.WorkflowTemplateVO;
import com.review.agent.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService governanceService;

    @GetMapping("/capabilities")
    public Result<List<GovernanceCapabilityVO>> listCapabilities() {
        return Result.success(governanceService.listCapabilities());
    }

    @GetMapping("/connectors")
    public Result<List<IntegrationConnectorVO>> listConnectors() {
        return Result.success(governanceService.listConnectors());
    }

    @GetMapping("/rule-packs")
    public Result<List<GovernanceRulePackVO>> listRulePacks() {
        return Result.success(governanceService.listRulePacks());
    }

    @GetMapping("/workflows")
    public Result<List<WorkflowTemplateVO>> listWorkflows() {
        return Result.success(governanceService.listWorkflows());
    }
}
