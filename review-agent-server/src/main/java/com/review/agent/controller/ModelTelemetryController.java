package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;
import com.review.agent.service.ModelTelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/model-telemetry")
@RequiredArgsConstructor
public class ModelTelemetryController {

    private final ModelTelemetryService modelTelemetryService;

    @PostMapping("/records")
    public Result<ModelCallTelemetryVO> record(@RequestBody ModelCallTelemetryRequest request) {
        return Result.success(modelTelemetryService.record(request));
    }

    @GetMapping("/summary")
    public Result<ModelTelemetrySummaryVO> summary() {
        return Result.success(modelTelemetryService.summary());
    }
}
