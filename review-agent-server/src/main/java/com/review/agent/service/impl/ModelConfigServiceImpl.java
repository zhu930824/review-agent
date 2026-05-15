package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.ModelProfileVO;
import com.review.agent.domain.dto.ModelProviderVO;
import com.review.agent.domain.dto.ReviewStrategyVO;
import com.review.agent.domain.dto.ReviewStrategyVO.GatePolicy;
import com.review.agent.domain.dto.StrategyRoleBindingVO;
import com.review.agent.domain.entity.ModelProfile;
import com.review.agent.domain.entity.ModelProvider;
import com.review.agent.domain.entity.ReviewStrategy;
import com.review.agent.domain.entity.ReviewStrategyModel;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.ModelProfileMapper;
import com.review.agent.infrastructure.persistence.ModelProviderMapper;
import com.review.agent.infrastructure.persistence.ReviewStrategyMapper;
import com.review.agent.infrastructure.persistence.ReviewStrategyModelMapper;
import com.review.agent.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模型配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    private final ModelProviderMapper modelProviderMapper;
    private final ModelProfileMapper modelProfileMapper;
    private final ReviewStrategyMapper reviewStrategyMapper;
    private final ReviewStrategyModelMapper reviewStrategyModelMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ModelProviderVO> listProviders() {
        List<ModelProvider> providers = modelProviderMapper.selectList(
                new LambdaQueryWrapper<ModelProvider>().eq(ModelProvider::getEnabled, true));

        return providers.stream().map(p -> {
            ModelProviderVO vo = new ModelProviderVO();
            vo.setId(p.getId());
            vo.setProviderKey(p.getProviderKey());
            vo.setName(p.getName());
            vo.setKind(p.getProviderType());
            vo.setEndpoint(p.getEndpoint());
            vo.setApiKeyEnv(p.getApiKeyEnv());
            vo.setEnabled(p.getEnabled());
            vo.setDescription(p.getDescription());
            vo.setCreatedAt(p.getCreatedAt());
            vo.setUpdatedAt(p.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ModelProfileVO> listProfiles() {
        List<ModelProfile> profiles = modelProfileMapper.selectList(
                new LambdaQueryWrapper<ModelProfile>().eq(ModelProfile::getEnabled, true));

        return profiles.stream().map(p -> {
            ModelProfileVO vo = new ModelProfileVO();
            vo.setId(p.getId());
            vo.setProviderId(p.getProviderId());
            vo.setProfileKey(p.getProfileKey());
            vo.setModelName(p.getModelName());
            vo.setDisplayName(p.getDisplayName());
            vo.setCapabilityTags(parseJsonArray(p.getCapabilityTags()));
            vo.setDefaultTemperature(p.getDefaultTemperature());
            vo.setTimeoutSeconds(p.getTimeoutSeconds());
            vo.setEnabled(p.getEnabled());
            vo.setCreatedAt(p.getCreatedAt());
            vo.setUpdatedAt(p.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ReviewStrategyVO> listStrategies() {
        // 1. 查询所有启用的策略
        List<ReviewStrategy> strategies = reviewStrategyMapper.selectList(
                new LambdaQueryWrapper<ReviewStrategy>().eq(ReviewStrategy::getEnabled, true));

        if (strategies.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 收集所有策略 ID，批量查询其下的模型绑定
        List<Long> strategyIds = strategies.stream().map(ReviewStrategy::getId).collect(Collectors.toList());
        List<ReviewStrategyModel> allBindings = reviewStrategyModelMapper.selectList(
                new LambdaQueryWrapper<ReviewStrategyModel>()
                        .in(ReviewStrategyModel::getStrategyId, strategyIds));

        // 3. 收集所有被引用的模型档案 ID，批量查询档案信息
        Set<Long> profileIds = allBindings.stream()
                .map(ReviewStrategyModel::getModelProfileId)
                .collect(Collectors.toSet());
        Map<Long, ModelProfile> profileMap = loadProfileMap(profileIds);

        // 4. 按策略 ID 分组绑定
        Map<Long, List<ReviewStrategyModel>> bindingsByStrategy = allBindings.stream()
                .collect(Collectors.groupingBy(ReviewStrategyModel::getStrategyId));

        // 5. 组装 ReviewStrategyVO
        return strategies.stream().map(s -> buildStrategyVO(s, bindingsByStrategy, profileMap)).collect(Collectors.toList());
    }

    // ---- 内部辅助方法 ----

    /** 组装单个策略的 VO */
    private ReviewStrategyVO buildStrategyVO(ReviewStrategy s,
                                              Map<Long, List<ReviewStrategyModel>> bindingsByStrategy,
                                              Map<Long, ModelProfile> profileMap) {
        ReviewStrategyVO vo = new ReviewStrategyVO();
        vo.setId(s.getId());
        vo.setStrategyKey(s.getStrategyKey());
        vo.setName(s.getName());
        vo.setReviewMode(s.getReviewMode());
        vo.setDescription(s.getDescription());
        vo.setRecommendedFor(parseJsonArray(s.getRecommendedFor()));
        vo.setGatePolicy(buildGatePolicy(s));
        vo.setEnabled(s.getEnabled());
        vo.setCreatedAt(s.getCreatedAt());
        vo.setUpdatedAt(s.getUpdatedAt());

        // 组装角色绑定
        List<ReviewStrategyModel> bindings = bindingsByStrategy.getOrDefault(s.getId(), Collections.emptyList());
        vo.setRoleBindings(bindings.stream()
                .map(b -> buildRoleBinding(b, profileMap))
                .collect(Collectors.toList()));

        return vo;
    }

    /** 组装单个角色绑定 VO */
    private StrategyRoleBindingVO buildRoleBinding(ReviewStrategyModel binding,
                                                    Map<Long, ModelProfile> profileMap) {
        StrategyRoleBindingVO vo = new StrategyRoleBindingVO();
        vo.setRole(binding.getRole());
        vo.setModelProfileId(binding.getModelProfileId());
        vo.setSkills(parseJsonArray(binding.getSkills()));
        vo.setTemperature(binding.getTemperature());

        ModelProfile profile = profileMap.get(binding.getModelProfileId());
        if (profile != null) {
            vo.setModelProfileName(profile.getDisplayName());
            vo.setModelName(profile.getModelName());
        }
        return vo;
    }

    /** 从策略实体构建 GatePolicy */
    private GatePolicy buildGatePolicy(ReviewStrategy s) {
        GatePolicy policy = new GatePolicy();
        policy.setBlockOn(parseSeverityArray(s.getBlockOn()));
        policy.setRequireHumanReviewOn(parseSeverityArray(s.getRequireHumanReviewOn()));
        policy.setAdvisoryOn(parseSeverityArray(s.getAdvisoryOn()));
        return policy;
    }

    /** 批量加载模型档案 Map */
    private Map<Long, ModelProfile> loadProfileMap(Set<Long> profileIds) {
        if (profileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ModelProfile> profiles = modelProfileMapper.selectList(
                new LambdaQueryWrapper<ModelProfile>().in(ModelProfile::getId, profileIds));
        return profiles.stream().collect(Collectors.toMap(ModelProfile::getId, p -> p));
    }

    /** 将 JSON 数组字符串解析为 List&lt;String&gt;，空值返回空列表 */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("JSON 数组解析失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    /** 将 JSON 数组字符串解析为 List&lt;Severity&gt;，空值返回空列表 */
    private List<Severity> parseSeverityArray(String json) {
        List<String> codes = parseJsonArray(json);
        return codes.stream()
                .map(code -> {
                    try {
                        return Severity.valueOf(code);
                    } catch (IllegalArgumentException e) {
                        log.warn("未知的 Severity 值: {}", code);
                        return null;
                    }
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }
}
