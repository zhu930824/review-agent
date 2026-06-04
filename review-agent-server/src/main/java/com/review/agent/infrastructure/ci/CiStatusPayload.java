package com.review.agent.infrastructure.ci;

public record CiStatusPayload(
        Long reviewId,
        CiStatusState state,
        String context,
        String description,
        String targetUrl,
        String repoUrl,
        String statusSha
) {
    public CiStatusPayload(
            Long reviewId,
            CiStatusState state,
            String context,
            String description,
            String targetUrl) {
        this(reviewId, state, context, description, targetUrl, "", "");
    }
}
