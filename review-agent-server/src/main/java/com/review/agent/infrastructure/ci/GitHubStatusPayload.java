package com.review.agent.infrastructure.ci;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubStatusPayload(
        String state,
        String context,
        String description,
        @JsonProperty("target_url") String targetUrl
) {
}
