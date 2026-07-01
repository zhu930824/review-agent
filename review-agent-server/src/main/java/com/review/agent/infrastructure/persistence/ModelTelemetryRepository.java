package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.ModelCallTelemetry;
import com.review.agent.domain.entity.ReviewFinding;

import java.util.List;

public interface ModelTelemetryRepository {

    ModelCallTelemetry insert(ModelCallTelemetry telemetry);

    List<ModelCallTelemetry> listRecent(int limit);

    List<ReviewFinding> listFindingsByReviewIds(List<Long> reviewIds);
}
