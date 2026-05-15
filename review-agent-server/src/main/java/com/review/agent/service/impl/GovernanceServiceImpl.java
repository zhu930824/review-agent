package com.review.agent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.GovernanceCapabilityVO;
import com.review.agent.domain.dto.GovernanceRulePackVO;
import com.review.agent.domain.dto.IntegrationConnectorVO;
import com.review.agent.domain.dto.WorkflowTemplateVO;
import com.review.agent.domain.entity.GovernanceCapability;
import com.review.agent.domain.entity.GovernanceRulePack;
import com.review.agent.domain.entity.IntegrationConnector;
import com.review.agent.domain.entity.WorkflowTemplate;
import com.review.agent.infrastructure.persistence.GovernanceCapabilityMapper;
import com.review.agent.infrastructure.persistence.GovernanceRulePackMapper;
import com.review.agent.infrastructure.persistence.IntegrationConnectorMapper;
import com.review.agent.infrastructure.persistence.WorkflowTemplateMapper;
import com.review.agent.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 治理中心服务实现 —— 查询数据库中的治理配置，并将 JSON 字段解析为 List
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceServiceImpl implements GovernanceService {

    private final GovernanceCapabilityMapper capabilityMapper;
    private final IntegrationConnectorMapper connectorMapper;
    private final GovernanceRulePackMapper rulePackMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;

    /** Jackson 解析器，用于将数据库 JSON 文本字段转换为 List */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<GovernanceCapabilityVO> listCapabilities() {
        List<GovernanceCapability> entities = capabilityMapper.selectList(null);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<IntegrationConnectorVO> listConnectors() {
        List<IntegrationConnector> entities = connectorMapper.selectList(null);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<GovernanceRulePackVO> listRulePacks() {
        List<GovernanceRulePack> entities = rulePackMapper.selectList(null);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<WorkflowTemplateVO> listWorkflows() {
        List<WorkflowTemplate> entities = workflowTemplateMapper.selectList(null);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    // ---- Entity -> VO 转换 ----

    private GovernanceCapabilityVO toVO(GovernanceCapability entity) {
        GovernanceCapabilityVO vo = new GovernanceCapabilityVO();
        vo.setId(entity.getId());
        vo.setCapabilityKey(entity.getCapabilityKey());
        vo.setName(entity.getName());
        vo.setCategory(entity.getCategory());
        vo.setStatus(entity.getStatus());
        vo.setBusinessImpact(entity.getBusinessImpact());
        vo.setReadiness(entity.getReadiness());
        vo.setMarketSignal(entity.getMarketSignal());
        vo.setPlatformMove(entity.getPlatformMove());
        vo.setSourceProducts(parseJsonList(entity.getSourceProducts()));
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private IntegrationConnectorVO toVO(IntegrationConnector entity) {
        IntegrationConnectorVO vo = new IntegrationConnectorVO();
        vo.setId(entity.getId());
        vo.setConnectorKey(entity.getConnectorKey());
        vo.setName(entity.getName());
        vo.setStage(entity.getRolloutStage());
        vo.setStatus(entity.getStatus());
        vo.setBusinessValue(entity.getBusinessValue());
        vo.setImplementationHint(entity.getImplementationHint());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private GovernanceRulePackVO toVO(GovernanceRulePack entity) {
        GovernanceRulePackVO vo = new GovernanceRulePackVO();
        vo.setId(entity.getId());
        vo.setRulePackKey(entity.getRulePackKey());
        vo.setName(entity.getName());
        vo.setBusinessOutcome(entity.getBusinessOutcome());
        vo.setControls(parseJsonList(entity.getControls()));
        vo.setCapabilityIds(parseJsonList(entity.getCapabilityKeys()));
        vo.setSuggestedStrategies(parseJsonList(entity.getStrategyKeys()));
        vo.setHumanCheckpoints(parseJsonList(entity.getHumanCheckpoints()));
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private WorkflowTemplateVO toVO(WorkflowTemplate entity) {
        WorkflowTemplateVO vo = new WorkflowTemplateVO();
        vo.setId(entity.getId());
        vo.setTemplateKey(entity.getTemplateKey());
        vo.setName(entity.getName());
        vo.setScenario(entity.getScenario());
        vo.setStrategyId(entity.getStrategyKey());
        vo.setSuccessMetric(entity.getSuccessMetric());
        vo.setEnabled(entity.getEnabled());
        vo.setRulePackIds(parseJsonList(entity.getRulePackKeys()));
        vo.setIntegrationIds(parseJsonList(entity.getConnectorKeys()));
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /**
     * 将 JSON 数组字符串解析为 List&lt;String&gt;；空值或解析失败返回空列表
     */
    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", json, e);
            return Collections.emptyList();
        }
    }
}
