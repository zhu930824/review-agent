package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.*;
import com.review.agent.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-config")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

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
}
