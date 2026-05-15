package com.review.agent.service;

import com.review.agent.domain.dto.ModelProfileVO;
import com.review.agent.domain.dto.ModelProviderVO;
import com.review.agent.domain.dto.ReviewStrategyVO;

import java.util.List;

/**
 * 模型配置服务 —— 提供供应商、模型档案、审查策略的只读查询
 */
public interface ModelConfigService {

    /** 查询所有启用的模型供应商 */
    List<ModelProviderVO> listProviders();

    /** 查询所有启用的模型档案 */
    List<ModelProfileVO> listProfiles();

    /** 查询所有启用的审查策略（含角色绑定与闸门策略） */
    List<ReviewStrategyVO> listStrategies();
}
