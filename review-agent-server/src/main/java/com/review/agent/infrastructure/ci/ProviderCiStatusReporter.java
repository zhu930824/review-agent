package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;

public interface ProviderCiStatusReporter extends CiStatusService {

    String connectorKey();

    default void reportPass(CiStatusConfig config, Long reviewId, String description) {
        reportPass(reviewId, description);
    }

    default void reportBlock(CiStatusConfig config, Long reviewId, String description) {
        reportBlock(reviewId, description);
    }

    default void reportRunning(CiStatusConfig config, Long reviewId, String description) {
        reportRunning(reviewId, description);
    }
}
