package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JenkinsGateRequestFactory {

    public CiProviderStatusRequest build(
            CiStatusConfig config,
            String commitSha,
            String state,
            String description,
            Long reviewId) {
        String url = buildJobUrl(config);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", authorizationHeader(config));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("REVIEW_AGENT_STATE", toJenkinsState(state));
        body.put("REVIEW_AGENT_CONTEXT", config.getStatusContext());
        body.put("REVIEW_AGENT_DESCRIPTION", description == null ? "" : description);
        body.put("REVIEW_AGENT_REVIEW_ID", String.valueOf(reviewId));
        if (commitSha != null && !commitSha.isBlank()) {
            body.put("REVIEW_AGENT_COMMIT_SHA", commitSha);
        }

        return new CiProviderStatusRequest(url, headers, body);
    }

    public String buildCrumbUrl(CiStatusConfig config) {
        return UriComponentsBuilder.fromUriString(trimTrailingSlash(config.getRepoUrl()))
                .pathSegment("crumbIssuer", "api", "json")
                .build()
                .toUriString();
    }

    public Map<String, String> buildCrumbHeaders(CiStatusConfig config) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", authorizationHeader(config));
        return headers;
    }

    public CiProviderStatusRequest withCrumb(CiProviderStatusRequest request, JenkinsCrumb crumb) {
        if (crumb == null || !hasText(crumb.field()) || !hasText(crumb.value())) {
            return request;
        }
        Map<String, String> headers = new LinkedHashMap<>(request.headers());
        headers.put(crumb.field(), crumb.value());
        return new CiProviderStatusRequest(request.url(), headers, request.body());
    }

    private String buildJobUrl(CiStatusConfig config) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(trimTrailingSlash(config.getRepoUrl()));
        if (hasText(config.getRepoOwner())) {
            for (String folder : config.getRepoOwner().split("/")) {
                if (hasText(folder)) {
                    builder.pathSegment("job", folder);
                }
            }
        }
        builder.pathSegment("job", config.getRepoName(), "buildWithParameters");
        return builder.build().toUriString();
    }

    private String authorizationHeader(CiStatusConfig config) {
        if (hasText(config.getWebhookSecret())) {
            String credentials = config.getWebhookSecret() + ":" + config.getApiToken();
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }
        return "Bearer " + config.getApiToken();
    }

    private String toJenkinsState(String state) {
        if ("success".equalsIgnoreCase(state)) {
            return "PASSED";
        }
        if ("failure".equalsIgnoreCase(state)) {
            return "BLOCKED";
        }
        return "RUNNING";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
