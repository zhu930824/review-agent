package com.review.agent.infrastructure.integration;

public record PrePrReportPublishPayload(
        Long reviewId,
        String format,
        String body
) {
}
