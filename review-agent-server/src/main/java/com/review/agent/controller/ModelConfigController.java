package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.*;
import com.review.agent.service.ModelInvocationPort;
import com.review.agent.service.ModelConfigService;
import com.review.agent.service.impl.HttpModelInvocationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-config")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;
    private final ModelInvocationPort modelInvocationPort;
    private final HttpModelInvocationProperties httpModelInvocationProperties;

    @GetMapping("/providers")
    public Result<List<ModelProviderVO>> listProviders() {
        return Result.success(modelConfigService.listProviders());
    }

    @GetMapping("/providers/{id}")
    public Result<ModelProviderVO> getProvider(@PathVariable("id") Long id) {
        return Result.success(modelConfigService.getProvider(id));
    }

    @PostMapping("/providers")
    public Result<ModelProviderVO> createProvider(@RequestBody ModelProviderVO request) {
        return Result.success(modelConfigService.createProvider(request));
    }

    @PutMapping("/providers/{id}")
    public Result<ModelProviderVO> updateProvider(@PathVariable("id") Long id, @RequestBody ModelProviderVO request) {
        return Result.success(modelConfigService.updateProvider(id, request));
    }

    @DeleteMapping("/providers/{id}")
    public Result<Void> deleteProvider(@PathVariable("id") Long id) {
        modelConfigService.deleteProvider(id);
        return Result.success();
    }

    @GetMapping("/profiles")
    public Result<List<ModelProfileVO>> listProfiles(@RequestParam(value = "providerId", required = false) Long providerId) {
        return Result.success(modelConfigService.listProfiles(providerId));
    }

    @GetMapping("/profiles/{id}")
    public Result<ModelProfileVO> getProfile(@PathVariable("id") Long id) {
        return Result.success(modelConfigService.getProfile(id));
    }

    @PostMapping("/profiles")
    public Result<ModelProfileVO> createProfile(@RequestBody ModelProfileVO request) {
        return Result.success(modelConfigService.createProfile(request));
    }

    @PutMapping("/profiles/{id}")
    public Result<ModelProfileVO> updateProfile(@PathVariable("id") Long id, @RequestBody ModelProfileVO request) {
        return Result.success(modelConfigService.updateProfile(id, request));
    }

    @DeleteMapping("/profiles/{id}")
    public Result<Void> deleteProfile(@PathVariable("id") Long id) {
        modelConfigService.deleteProfile(id);
        return Result.success();
    }

    @GetMapping("/strategies")
    public Result<List<ReviewStrategyVO>> listStrategies() {
        return Result.success(modelConfigService.listStrategies());
    }

    @GetMapping("/strategies/{id}")
    public Result<ReviewStrategyVO> getStrategy(@PathVariable("id") Long id) {
        return Result.success(modelConfigService.getStrategy(id));
    }

    @PostMapping("/strategies")
    public Result<ReviewStrategyVO> createStrategy(@RequestBody ReviewStrategyVO request) {
        return Result.success(modelConfigService.createStrategy(request));
    }

    @PutMapping("/strategies/{id}")
    public Result<ReviewStrategyVO> updateStrategy(@PathVariable("id") Long id, @RequestBody ReviewStrategyVO request) {
        return Result.success(modelConfigService.updateStrategy(id, request));
    }

    @DeleteMapping("/strategies/{id}")
    public Result<Void> deleteStrategy(@PathVariable("id") Long id) {
        modelConfigService.deleteStrategy(id);
        return Result.success();
    }

    @PostMapping("/invocations/smoke-test")
    public Result<ModelInvocationSmokeTestVO> smokeTestInvocation(@RequestBody ModelInvocationSmokeTestRequest request) {
        ModelInvocationRequest invocationRequest = toInvocationRequest(request);
        ModelInvocationSmokeTestVO result = new ModelInvocationSmokeTestVO();
        result.setProvider(invocationRequest.getProvider());
        result.setModelName(invocationRequest.getModelName());
        result.setPromptVersion(invocationRequest.getPromptVersion());
        try {
            ModelInvocationResponse response = modelInvocationPort.invoke(invocationRequest);
            result.setStatus("SUCCESS");
            result.setContent(response == null ? null : response.getContent());
            result.setPromptTokens(response == null ? null : response.getPromptTokens());
            result.setCompletionTokens(response == null ? null : response.getCompletionTokens());
            result.setCostMicroCents(response == null ? null : response.getCostMicroCents());
        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }
        return Result.success(result);
    }

    private ModelInvocationRequest toInvocationRequest(ModelInvocationSmokeTestRequest request) {
        ModelInvocationRequest invocationRequest = new ModelInvocationRequest();
        if (request != null) {
            invocationRequest.setReviewId(request.getReviewId());
            invocationRequest.setStrategyKey(request.getStrategyKey());
            invocationRequest.setProvider(request.getProvider());
            invocationRequest.setModelName(request.getModelName());
            invocationRequest.setRole(request.getRole());
            invocationRequest.setPromptVersion(request.getPromptVersion());
            invocationRequest.setPrompt(request.getPrompt());
            invocationRequest.setTemperature(request.getTemperature());
        }
        if (!StringUtils.hasText(invocationRequest.getStrategyKey())) {
            invocationRequest.setStrategyKey("smoke-test");
        }
        if (!StringUtils.hasText(invocationRequest.getProvider())) {
            invocationRequest.setProvider(httpModelInvocationProperties.getProvider());
        }
        if (!StringUtils.hasText(invocationRequest.getModelName())) {
            invocationRequest.setModelName(httpModelInvocationProperties.getModelName());
        }
        if (!StringUtils.hasText(invocationRequest.getRole())) {
            invocationRequest.setRole("OPERATOR");
        }
        if (!StringUtils.hasText(invocationRequest.getPromptVersion())) {
            invocationRequest.setPromptVersion("model-config-smoke-test-v1");
        }
        if (!StringUtils.hasText(invocationRequest.getPrompt())) {
            invocationRequest.setPrompt("Reply with OK to confirm the model invocation path is configured.");
        }
        return invocationRequest;
    }
}
