package com.review.agent.service;

import com.review.agent.domain.dto.ModelProfileVO;
import com.review.agent.domain.dto.ModelProviderVO;
import com.review.agent.domain.dto.ReviewStrategyVO;

import java.util.List;

public interface ModelConfigService {

    // ModelProvider
    List<ModelProviderVO> listProviders();
    ModelProviderVO getProvider(Long id);
    ModelProviderVO createProvider(ModelProviderVO request);
    ModelProviderVO updateProvider(Long id, ModelProviderVO request);
    void deleteProvider(Long id);

    // ModelProfile
    List<ModelProfileVO> listProfiles(Long providerId);
    ModelProfileVO getProfile(Long id);
    ModelProfileVO createProfile(ModelProfileVO request);
    ModelProfileVO updateProfile(Long id, ModelProfileVO request);
    void deleteProfile(Long id);

    // ReviewStrategy
    List<ReviewStrategyVO> listStrategies();
    ReviewStrategyVO getStrategy(Long id);
    ReviewStrategyVO createStrategy(ReviewStrategyVO request);
    ReviewStrategyVO updateStrategy(Long id, ReviewStrategyVO request);
    void deleteStrategy(Long id);
}
