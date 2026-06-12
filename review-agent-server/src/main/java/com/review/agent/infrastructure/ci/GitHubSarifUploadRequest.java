package com.review.agent.infrastructure.ci;

import java.util.Map;

public record GitHubSarifUploadRequest(
        String url,
        Map<String, String> headers,
        Map<String, Object> body
) {
}
