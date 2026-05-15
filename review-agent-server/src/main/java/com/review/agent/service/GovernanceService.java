package com.review.agent.service;

import com.review.agent.domain.dto.GovernanceCapabilityVO;
import com.review.agent.domain.dto.GovernanceRulePackVO;
import com.review.agent.domain.dto.IntegrationConnectorVO;
import com.review.agent.domain.dto.WorkflowTemplateVO;

import java.util.List;

/**
 * 治理中心服务接口 —— 提供能力目录、连接器路线图、规则包、工作流模板的查询
 */
public interface GovernanceService {

    List<GovernanceCapabilityVO> listCapabilities();

    List<IntegrationConnectorVO> listConnectors();

    List<GovernanceRulePackVO> listRulePacks();

    List<WorkflowTemplateVO> listWorkflows();
}
