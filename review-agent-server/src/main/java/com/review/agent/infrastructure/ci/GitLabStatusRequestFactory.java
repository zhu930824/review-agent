package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GitLabStatusRequestFactory {

    private static final int DESCRIPTION_LIMIT = 140;

    public CiProviderStatusRequest build(
            CiStatusConfig config,
            String commitSha,
            String state,
            String description,
            Long reviewId) {
        String projectPath = resolveProjectPath(config);
        String baseUrl = resolveBaseUrl(config.getRepoUrl());
        String encodedProjectPath = URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
        String url = "%s/api/v4/projects/%s/statuses/%s".formatted(baseUrl, encodedProjectPath, commitSha);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("PRIVATE-TOKEN", config.getApiToken());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("state", toGitLabState(state));
        body.put("name", config.getStatusContext());
        body.put("description", truncate(description));
        body.put("target_url", "/reviews/" + reviewId);

        return new CiProviderStatusRequest(url, headers, body);
    }

    private String resolveProjectPath(CiStatusConfig config) {
        if (hasText(config.getRepoOwner()) && hasText(config.getRepoName())) {
            return config.getRepoOwner() + "/" + config.getRepoName();
        }
        URI uri = URI.create(config.getRepoUrl());
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("GitLab project path is missing");
        }
        return trimGitSuffix(path.replaceFirst("^/", ""));
    }

    private String resolveBaseUrl(String repoUrl) {
        URI uri = URI.create(repoUrl);
        return UriComponentsBuilder.newInstance()
                .scheme(uri.getScheme())
                .host(uri.getHost())
                .port(uri.getPort())
                .build()
                .toUriString();
    }

    private String toGitLabState(String state) {
        if ("success".equalsIgnoreCase(state)) {
            return "success";
        }
        if ("failure".equalsIgnoreCase(state)) {
            return "failed";
        }
        return "pending";
    }

    private String trimGitSuffix(String value) {
        return value.endsWith(".git") ? value.substring(0, value.length() - 4) : value;
    }

    private String truncate(String description) {
        if (description == null) {
            return "";
        }
        if (description.length() <= DESCRIPTION_LIMIT) {
            return description;
        }
        return description.substring(0, DESCRIPTION_LIMIT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
