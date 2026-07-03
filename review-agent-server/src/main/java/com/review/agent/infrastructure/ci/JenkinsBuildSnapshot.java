package com.review.agent.infrastructure.ci;

public record JenkinsBuildSnapshot(
        String buildUrl,
        String buildNumber,
        String result,
        boolean building
) {
}
