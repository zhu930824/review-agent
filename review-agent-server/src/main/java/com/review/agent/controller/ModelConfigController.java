package com.review.agent.controller;

import com.review.agent.domain.dto.BaseResult;
import com.review.agent.domain.dto.ModelProfileVO;
import com.review.agent.domain.dto.ModelProviderVO;
import com.review.agent.domain.dto.ReviewStrategyVO;
import com.review.agent.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型配置查询接口 —— 为前端策略中心、模型管理页面提供数据
 */
@RestController
@RequestMapping("/api/model-config")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    /** 获取所有启用的模型供应商 */
    @GetMapping("/providers")
    public BaseResult<List<ModelProviderVO>> listProviders() {
        return BaseResult.success(modelConfigService.listProviders());
    }

    /** 获取所有启用的模型档案（含能力标签） */
    @GetMapping("/profiles")
    public BaseResult<List<ModelProfileVO>> listProfiles() {
        return BaseResult.success(modelConfigService.listProfiles());
    }

    /** 获取所有启用的审查策略（含闸门策略和角色绑定） */
    @GetMapping("/strategies")
    public BaseResult<List<ReviewStrategyVO>> listStrategies() {
        return BaseResult.success(modelConfigService.listStrategies());
    }
}
