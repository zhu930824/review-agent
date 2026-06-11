package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.infrastructure.ci.PrePrGateCiStatusPublisher;
import com.review.agent.service.PrePrGateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PrePrGateController {

    private final PrePrGateService prePrGateService;
    private final PrePrGateCiStatusPublisher prePrGateCiStatusPublisher;

    @GetMapping("/{id}/gate")
    public Result<PrePrGateVO> getGate(@PathVariable("id") Long reviewId) {
        return Result.success(prePrGateService.getGate(reviewId));
    }

    @PostMapping("/{id}/gate/initialize")
    public Result<PrePrGateVO> initializeGate(@PathVariable("id") Long reviewId) {
        return Result.success(prePrGateService.initializeGate(reviewId));
    }

    @PostMapping("/{id}/gate/refresh")
    public Result<PrePrGateVO> refreshGate(@PathVariable("id") Long reviewId) {
        PrePrGateVO gate = prePrGateService.refreshGate(reviewId);
        prePrGateCiStatusPublisher.publishGateStatus(reviewId);
        return Result.success(gate);
    }

    @PatchMapping("/{id}/pre-pr-decision")
    public Result<PrePrGateVO> decideGate(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody PrePrGateDecisionRequest request) {
        PrePrGateVO gate = prePrGateService.decideGate(reviewId, request);
        prePrGateCiStatusPublisher.publishGateStatus(reviewId);
        return Result.success(gate);
    }

    @PostMapping("/{id}/gate/publish-ci")
    public Result<Void> publishCiStatus(@PathVariable("id") Long reviewId) {
        prePrGateCiStatusPublisher.publishGateStatus(reviewId);
        return Result.success();
    }
}
