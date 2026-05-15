package com.review.agent.controller;

import com.review.agent.domain.dto.BaseResult;
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

/**
 * 治理中心控制器 —— 提供能力目录、连接器路线图、规则包、工作流模板的查询接口
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService governanceService;

    @GetMapping("/capabilities")
    public BaseResult<List<GovernanceCapabilityVO>> listCapabilities() {
        return BaseResult.success(governanceService.listCapabilities());
    }

    @GetMapping("/connectors")
    public BaseResult<List<IntegrationConnectorVO>> listConnectors() {
        return BaseResult.success(governanceService.listConnectors());
    }

    @GetMapping("/rule-packs")
    public BaseResult<List<GovernanceRulePackVO>> listRulePacks() {
        return BaseResult.success(governanceService.listRulePacks());
    }

    @GetMapping("/workflows")
    public BaseResult<List<WorkflowTemplateVO>> listWorkflows() {
        return BaseResult.success(governanceService.listWorkflows());
    }
}
