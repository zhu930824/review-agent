package com.review.agent.service;

import com.review.agent.domain.dto.OperationsTelemetryReadinessVO;

import java.util.List;

public interface OperationsTelemetryReadinessService {

    List<OperationsTelemetryReadinessVO> listReadiness();
}
