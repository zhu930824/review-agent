package com.review.agent.infrastructure.ci;

public record JenkinsQueueSnapshot(
        String queueUrl,
        String buildUrl,
        String buildNumber,
        String result
) {
}
