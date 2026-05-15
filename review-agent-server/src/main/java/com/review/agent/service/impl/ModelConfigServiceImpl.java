package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.*;
import com.review.agent.domain.entity.ModelProfile;
import com.review.agent.domain.entity.ModelProvider;
import com.review.agent.domain.entity.ReviewStrategy;
import com.review.agent.domain.entity.ReviewStrategyModel;
import com.review.agent.domain.exception.BusinessException;
import com.review.agent.infrastructure.persistence.ModelProfileMapper;
import com.review.agent.infrastructure.persistence.ModelProviderMapper;
import com.review.agent.infrastructure.persistence.ReviewStrategyMapper;
import com.review.agent.infrastructure.persistence.ReviewStrategyModelMapper;
import com.review.agent.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    private final ModelProviderMapper modelProviderMapper;
    private final ModelProfileMapper modelProfileMapper;
    private final ReviewStrategyMapper reviewStrategyMapper;
    private final ReviewStrategyModelMapper reviewStrategyModelMapper;
    private final ObjectMapper objectMapper;

    // ==================== ModelProvider ====================

    @Override
    public List<ModelProviderVO> listProviders() {
        List<ModelProvider> providers = modelProviderMapper.selectList(
                new LambdaQueryWrapper<ModelProvider>().orderByAsc(ModelProvider::getId));
        return providers.stream().map(this::toProviderVO).toList();
    }

    @Override
    public ModelProviderVO getProvider(Long id) {
        ModelProvider provider = requireProvider(id);
        ModelProviderVO vo = toProviderVO(provider);
        vo.setProfiles(listProfiles(id));
        return vo;
    }

    @Override
    @Transactional
    public ModelProviderVO createProvider(ModelProviderVO request) {
        ModelProvider provider = new ModelProvider();
        copyProviderFields(provider, request);
        modelProviderMapper.insert(provider);
        return toProviderVO(provider);
    }

    @Override
    @Transactional
    public ModelProviderVO updateProvider(Long id, ModelProviderVO request) {
        ModelProvider provider = requireProvider(id);
        copyProviderFields(provider, request);
        modelProviderMapper.updateById(provider);
        return toProviderVO(provider);
    }

    @Override
    @Transactional
    public void deleteProvider(Long id) {
        requireProvider(id);
        modelProviderMapper.deleteById(id);
    }

    // ==================== ModelProfile ====================

    @Override
    public List<ModelProfileVO> listProfiles(Long providerId) {
        LambdaQueryWrapper<ModelProfile> wrapper = new LambdaQueryWrapper<>();
        if (providerId != null) {
            wrapper.eq(ModelProfile::getProviderId, providerId);
        }
        wrapper.orderByAsc(ModelProfile::getId);
        List<ModelProfile> profiles = modelProfileMapper.selectList(wrapper);
        return profiles.stream().map(this::toProfileVO).toList();
    }

    @Override
    public ModelProfileVO getProfile(Long id) {
        ModelProfile profile = requireProfile(id);
        return toProfileVO(profile);
    }

    @Override
    @Transactional
    public ModelProfileVO createProfile(ModelProfileVO request) {
        ModelProfile profile = new ModelProfile();
        copyProfileFields(profile, request);
        modelProfileMapper.insert(profile);
        return toProfileVO(profile);
    }

    @Override
    @Transactional
    public ModelProfileVO updateProfile(Long id, ModelProfileVO request) {
        ModelProfile profile = requireProfile(id);
        copyProfileFields(profile, request);
        modelProfileMapper.updateById(profile);
        return toProfileVO(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        requireProfile(id);
        modelProfileMapper.deleteById(id);
    }

    // ==================== ReviewStrategy ====================

    @Override
    public List<ReviewStrategyVO> listStrategies() {
        List<ReviewStrategy> strategies = reviewStrategyMapper.selectList(
                new LambdaQueryWrapper<ReviewStrategy>().orderByAsc(ReviewStrategy::getId));
        return strategies.stream().map(s -> {
            ReviewStrategyVO vo = toStrategyVO(s);
            vo.setRoleBindings(loadRoleBindings(s.getId()));
            return vo;
        }).toList();
    }

    @Override
    public ReviewStrategyVO getStrategy(Long id) {
        ReviewStrategy strategy = requireStrategy(id);
        ReviewStrategyVO vo = toStrategyVO(strategy);
        vo.setRoleBindings(loadRoleBindings(id));
        return vo;
    }

    @Override
    @Transactional
    public ReviewStrategyVO createStrategy(ReviewStrategyVO request) {
        ReviewStrategy strategy = new ReviewStrategy();
        copyStrategyFields(strategy, request);
        reviewStrategyMapper.insert(strategy);
        if (request.getRoleBindings() != null) {
            saveRoleBindings(strategy.getId(), request.getRoleBindings());
        }
        return getStrategy(strategy.getId());
    }

    @Override
    @Transactional
    public ReviewStrategyVO updateStrategy(Long id, ReviewStrategyVO request) {
        ReviewStrategy strategy = requireStrategy(id);
        copyStrategyFields(strategy, request);
        reviewStrategyMapper.updateById(strategy);
        reviewStrategyModelMapper.delete(
                new LambdaQueryWrapper<ReviewStrategyModel>().eq(ReviewStrategyModel::getStrategyId, id));
        if (request.getRoleBindings() != null) {
            saveRoleBindings(id, request.getRoleBindings());
        }
        return getStrategy(id);
    }

    @Override
    @Transactional
    public void deleteStrategy(Long id) {
        requireStrategy(id);
        reviewStrategyMapper.deleteById(id);
        reviewStrategyModelMapper.delete(
                new LambdaQueryWrapper<ReviewStrategyModel>().eq(ReviewStrategyModel::getStrategyId, id));
    }

    // ==================== Helper Methods ====================

    private ModelProvider requireProvider(Long id) {
        ModelProvider provider = modelProviderMapper.selectById(id);
        if (provider == null) {
            throw new BusinessException("404", "模型供应商不存在: " + id);
        }
        return provider;
    }

    private ModelProfile requireProfile(Long id) {
        ModelProfile profile = modelProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException("404", "模型档案不存在: " + id);
        }
        return profile;
    }

    private ReviewStrategy requireStrategy(Long id) {
        ReviewStrategy strategy = reviewStrategyMapper.selectById(id);
        if (strategy == null) {
            throw new BusinessException("404", "审查策略不存在: " + id);
        }
        return strategy;
    }

    private ModelProviderVO toProviderVO(ModelProvider entity) {
        ModelProviderVO vo = new ModelProviderVO();
        vo.setId(entity.getId());
        vo.setProviderKey(entity.getProviderKey());
        vo.setName(entity.getName());
        vo.setProviderType(entity.getProviderType());
        vo.setEndpoint(entity.getEndpoint());
        vo.setApiKeyEnv(entity.getApiKeyEnv());
        vo.setEnabled(entity.getEnabled());
        vo.setDescription(entity.getDescription());
        return vo;
    }

    private void copyProviderFields(ModelProvider entity, ModelProviderVO vo) {
        if (vo.getProviderKey() != null) entity.setProviderKey(vo.getProviderKey());
        if (vo.getName() != null) entity.setName(vo.getName());
        if (vo.getProviderType() != null) entity.setProviderType(vo.getProviderType());
        if (vo.getEndpoint() != null) entity.setEndpoint(vo.getEndpoint());
        if (vo.getApiKeyEnv() != null) entity.setApiKeyEnv(vo.getApiKeyEnv());
        if (vo.getEnabled() != null) entity.setEnabled(vo.getEnabled());
        if (vo.getDescription() != null) entity.setDescription(vo.getDescription());
    }

    private ModelProfileVO toProfileVO(ModelProfile entity) {
        ModelProfileVO vo = new ModelProfileVO();
        vo.setId(entity.getId());
        vo.setProviderId(entity.getProviderId());
        vo.setProfileKey(entity.getProfileKey());
        vo.setModelName(entity.getModelName());
        vo.setDisplayName(entity.getDisplayName());
        vo.setDefaultTemperature(entity.getDefaultTemperature());
        vo.setTimeoutSeconds(entity.getTimeoutSeconds());
        vo.setEnabled(entity.getEnabled());
        if (entity.getCapabilityTags() != null) {
            vo.setCapabilityTags(parseJsonList(entity.getCapabilityTags()));
        }
        ModelProvider provider = modelProviderMapper.selectById(entity.getProviderId());
        if (provider != null) {
            vo.setProviderName(provider.getName());
        }
        return vo;
    }

    private void copyProfileFields(ModelProfile entity, ModelProfileVO vo) {
        if (vo.getProviderId() != null) entity.setProviderId(vo.getProviderId());
        if (vo.getProfileKey() != null) entity.setProfileKey(vo.getProfileKey());
        if (vo.getModelName() != null) entity.setModelName(vo.getModelName());
        if (vo.getDisplayName() != null) entity.setDisplayName(vo.getDisplayName());
        if (vo.getDefaultTemperature() != null) entity.setDefaultTemperature(vo.getDefaultTemperature());
        if (vo.getTimeoutSeconds() != null) entity.setTimeoutSeconds(vo.getTimeoutSeconds());
        if (vo.getEnabled() != null) entity.setEnabled(vo.getEnabled());
        if (vo.getCapabilityTags() != null) {
            entity.setCapabilityTags(toJson(vo.getCapabilityTags()));
        }
    }

    private ReviewStrategyVO toStrategyVO(ReviewStrategy entity) {
        ReviewStrategyVO vo = new ReviewStrategyVO();
        vo.setId(entity.getId());
        vo.setStrategyKey(entity.getStrategyKey());
        vo.setName(entity.getName());
        vo.setReviewMode(entity.getReviewMode());
        vo.setDescription(entity.getDescription());
        vo.setEnabled(entity.getEnabled());
        if (entity.getRecommendedFor() != null) {
            vo.setRecommendedFor(parseJsonList(entity.getRecommendedFor()));
        }
        if (entity.getBlockOn() != null) {
            vo.setBlockOn(parseJsonList(entity.getBlockOn()));
        }
        if (entity.getRequireHumanReviewOn() != null) {
            vo.setRequireHumanReviewOn(parseJsonList(entity.getRequireHumanReviewOn()));
        }
        if (entity.getAdvisoryOn() != null) {
            vo.setAdvisoryOn(parseJsonList(entity.getAdvisoryOn()));
        }
        return vo;
    }

    private void copyStrategyFields(ReviewStrategy entity, ReviewStrategyVO vo) {
        if (vo.getStrategyKey() != null) entity.setStrategyKey(vo.getStrategyKey());
        if (vo.getName() != null) entity.setName(vo.getName());
        if (vo.getReviewMode() != null) entity.setReviewMode(vo.getReviewMode());
        if (vo.getDescription() != null) entity.setDescription(vo.getDescription());
        if (vo.getEnabled() != null) entity.setEnabled(vo.getEnabled());
        if (vo.getRecommendedFor() != null) entity.setRecommendedFor(toJson(vo.getRecommendedFor()));
        if (vo.getBlockOn() != null) entity.setBlockOn(toJson(vo.getBlockOn()));
        if (vo.getRequireHumanReviewOn() != null) entity.setRequireHumanReviewOn(toJson(vo.getRequireHumanReviewOn()));
        if (vo.getAdvisoryOn() != null) entity.setAdvisoryOn(toJson(vo.getAdvisoryOn()));
    }

    private List<RoleBindingVO> loadRoleBindings(Long strategyId) {
        List<ReviewStrategyModel> bindings = reviewStrategyModelMapper.selectList(
                new LambdaQueryWrapper<ReviewStrategyModel>()
                        .eq(ReviewStrategyModel::getStrategyId, strategyId)
                        .orderByAsc(ReviewStrategyModel::getSortOrder));
        List<RoleBindingVO> result = new ArrayList<>();
        for (ReviewStrategyModel binding : bindings) {
            RoleBindingVO vo = new RoleBindingVO();
            vo.setId(binding.getId());
            vo.setRole(binding.getRole());
            vo.setModelProfileId(binding.getModelProfileId());
            vo.setTemperature(binding.getTemperature());
            if (binding.getSkills() != null) {
                vo.setSkills(parseJsonList(binding.getSkills()));
            }
            ModelProfile profile = modelProfileMapper.selectById(binding.getModelProfileId());
            if (profile != null) {
                vo.setModelProfileName(profile.getDisplayName());
            }
            result.add(vo);
        }
        return result;
    }

    private void saveRoleBindings(Long strategyId, List<RoleBindingVO> bindings) {
        int order = 0;
        for (RoleBindingVO vo : bindings) {
            ReviewStrategyModel entity = new ReviewStrategyModel();
            entity.setStrategyId(strategyId);
            entity.setRole(vo.getRole());
            entity.setModelProfileId(vo.getModelProfileId());
            entity.setTemperature(vo.getTemperature());
            entity.setSortOrder(order++);
            if (vo.getSkills() != null) {
                entity.setSkills(toJson(vo.getSkills()));
            }
            reviewStrategyModelMapper.insert(entity);
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析 JSON 列表失败: {}", json, e);
            return List.of();
        }
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("序列化列表失败", e);
            return "[]";
        }
    }
}
