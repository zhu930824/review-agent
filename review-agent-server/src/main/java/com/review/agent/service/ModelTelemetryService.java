package com.review.agent.service;

import com.review.agent.domain.dto.ModelCallTelemetryRequest;
import com.review.agent.domain.dto.ModelCallTelemetryVO;
import com.review.agent.domain.dto.ModelTelemetrySummaryVO;

public interface ModelTelemetryService {

    ModelCallTelemetryVO record(ModelCallTelemetryRequest request);

    ModelTelemetrySummaryVO summary();
}
