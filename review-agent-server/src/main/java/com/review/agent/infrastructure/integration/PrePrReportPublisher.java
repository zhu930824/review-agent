package com.review.agent.infrastructure.integration;

public interface PrePrReportPublisher {

    boolean publish(Long reviewId, String markdown);
}
