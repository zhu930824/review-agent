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
        String jenkinsState = toJenkinsState(state);
        body.put("REVIEW_AGENT_STATE", jenkinsState);
        body.put("REVIEW_AGENT_CONTEXT", config.getStatusContext());
        body.put("REVIEW_AGENT_DESCRIPTION", description == null ? "" : description);
        body.put("REVIEW_AGENT_REVIEW_ID", String.valueOf(reviewId));
        if (commitSha != null && !commitSha.isBlank()) {
            body.put("REVIEW_AGENT_COMMIT_SHA", commitSha);
        }
        body.putAll(resolveParameterTemplate(config, commitSha, state, jenkinsState, description, reviewId));

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

    public String buildJobApiUrl(CiStatusConfig config) {
        UriComponentsBuilder builder = jobUrlBuilder(config);
        builder.pathSegment("api", "json");
        return builder.queryParam("tree", "name,url,buildable,color").build().toUriString();
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
        return jobUrlBuilder(config).pathSegment("buildWithParameters").build().toUriString();
    }

    private UriComponentsBuilder jobUrlBuilder(CiStatusConfig config) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(trimTrailingSlash(config.getRepoUrl()));
        if (hasText(config.getRepoOwner())) {
            for (String folder : config.getRepoOwner().split("/")) {
                if (hasText(folder)) {
                    builder.pathSegment("job", folder);
                }
            }
        }
        builder.pathSegment("job", config.getRepoName());
        return builder;
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

    private Map<String, Object> resolveParameterTemplate(
            CiStatusConfig config,
            String commitSha,
            String state,
            String jenkinsState,
            String description,
            Long reviewId) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (!hasText(config.getJenkinsParameterTemplate())) {
            return parameters;
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("reviewId", reviewId == null ? "" : String.valueOf(reviewId));
        values.put("state", state == null ? "" : state);
        values.put("jenkinsState", jenkinsState == null ? "" : jenkinsState);
        values.put("commitSha", commitSha == null ? "" : commitSha);
        values.put("context", config.getStatusContext() == null ? "" : config.getStatusContext());
        values.put("description", description == null ? "" : description);
        values.put("connectorKey", config.getConnectorKey() == null ? "" : config.getConnectorKey());
        values.put("repoOwner", config.getRepoOwner() == null ? "" : config.getRepoOwner());
        values.put("repoName", config.getRepoName() == null ? "" : config.getRepoName());
        values.put("defaultBranch", config.getDefaultBranch() == null ? "" : config.getDefaultBranch());

        for (String item : config.getJenkinsParameterTemplate().split("[\\r\\n&]+")) {
            if (!hasText(item) || !item.contains("=")) {
                continue;
            }
            String[] parts = item.split("=", 2);
            String key = parts[0].trim();
            if (!hasText(key)) {
                continue;
            }
            parameters.put(key, replacePlaceholders(parts.length > 1 ? parts[1].trim() : "", values));
        }
        return parameters;
    }

    private String replacePlaceholders(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
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
