package com.review.agent.infrastructure.ci;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitLabStatusPayload(
        String state,
        String name,
        String description,
        @JsonProperty("target_url") String targetUrl
) {
}
