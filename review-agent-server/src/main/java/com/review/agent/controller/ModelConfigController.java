package com.review.agent.controller;

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

    // ==================== ModelProvider ====================

    @GetMapping("/providers")
    public ApiResponse<List<ModelProviderVO>> listProviders() {
        return ApiResponse.success(modelConfigService.listProviders());
    }

    @GetMapping("/providers/{id}")
    public ApiResponse<ModelProviderVO> getProvider(@PathVariable("id") Long id) {
        return ApiResponse.success(modelConfigService.getProvider(id));
    }

    @PostMapping("/providers")
    public ApiResponse<ModelProviderVO> createProvider(@RequestBody ModelProviderVO request) {
        return ApiResponse.success(modelConfigService.createProvider(request));
    }

    @PutMapping("/providers/{id}")
    public ApiResponse<ModelProviderVO> updateProvider(@PathVariable("id") Long id, @RequestBody ModelProviderVO request) {
        return ApiResponse.success(modelConfigService.updateProvider(id, request));
    }

    @DeleteMapping("/providers/{id}")
    public ApiResponse<Void> deleteProvider(@PathVariable("id") Long id) {
        modelConfigService.deleteProvider(id);
        return ApiResponse.success();
    }

    // ==================== ModelProfile ====================

    @GetMapping("/profiles")
    public ApiResponse<List<ModelProfileVO>> listProfiles(@RequestParam(value = "providerId", required = false) Long providerId) {
        return ApiResponse.success(modelConfigService.listProfiles(providerId));
    }

    @GetMapping("/profiles/{id}")
    public ApiResponse<ModelProfileVO> getProfile(@PathVariable("id") Long id) {
        return ApiResponse.success(modelConfigService.getProfile(id));
    }

    @PostMapping("/profiles")
    public ApiResponse<ModelProfileVO> createProfile(@RequestBody ModelProfileVO request) {
        return ApiResponse.success(modelConfigService.createProfile(request));
    }

    @PutMapping("/profiles/{id}")
    public ApiResponse<ModelProfileVO> updateProfile(@PathVariable("id") Long id, @RequestBody ModelProfileVO request) {
        return ApiResponse.success(modelConfigService.updateProfile(id, request));
    }

    @DeleteMapping("/profiles/{id}")
    public ApiResponse<Void> deleteProfile(@PathVariable("id") Long id) {
        modelConfigService.deleteProfile(id);
        return ApiResponse.success();
    }

    // ==================== ReviewStrategy ====================

    @GetMapping("/strategies")
    public ApiResponse<List<ReviewStrategyVO>> listStrategies() {
        return ApiResponse.success(modelConfigService.listStrategies());
    }

    @GetMapping("/strategies/{id}")
    public ApiResponse<ReviewStrategyVO> getStrategy(@PathVariable("id") Long id) {
        return ApiResponse.success(modelConfigService.getStrategy(id));
    }

    @PostMapping("/strategies")
    public ApiResponse<ReviewStrategyVO> createStrategy(@RequestBody ReviewStrategyVO request) {
        return ApiResponse.success(modelConfigService.createStrategy(request));
    }

    @PutMapping("/strategies/{id}")
    public ApiResponse<ReviewStrategyVO> updateStrategy(@PathVariable("id") Long id, @RequestBody ReviewStrategyVO request) {
        return ApiResponse.success(modelConfigService.updateStrategy(id, request));
    }

    @DeleteMapping("/strategies/{id}")
    public ApiResponse<Void> deleteStrategy(@PathVariable("id") Long id) {
        modelConfigService.deleteStrategy(id);
        return ApiResponse.success();
    }
}
