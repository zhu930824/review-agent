package com.review.agent.infrastructure.ci;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CiStatusEndpointFactory {

    private final String apiBaseUrl;

    public CiStatusEndpointFactory(String apiBaseUrl) {
        this.apiBaseUrl = trimTrailingSlash(apiBaseUrl);
    }

    public CiStatusEndpoint generic(String endpoint) {
        return new CiStatusEndpoint(endpoint == null ? "" : endpoint);
    }

    public CiStatusEndpoint github(String repoUrl, String sha) {
        RepositoryCoordinates coordinates = RepositoryCoordinates.parse(repoUrl);
        return new CiStatusEndpoint("%s/repos/%s/%s/statuses/%s".formatted(
                apiBaseUrl,
                coordinates.owner(),
                coordinates.repo(),
                sha));
    }

    public CiStatusEndpoint gitlab(String repoUrl, String sha) {
        RepositoryCoordinates coordinates = RepositoryCoordinates.parse(repoUrl);
        String projectPath = URLEncoder.encode(
                coordinates.projectPath(),
                StandardCharsets.UTF_8).replace("+", "%20");
        return new CiStatusEndpoint("%s/projects/%s/statuses/%s".formatted(apiBaseUrl, projectPath, sha));
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record RepositoryCoordinates(String owner, String repo, String projectPath) {

        static RepositoryCoordinates parse(String repoUrl) {
            String path = repoUrl == null ? "" : repoUrl.trim();
            int colonIndex = path.indexOf(':');
            if (path.startsWith("git@") && colonIndex >= 0) {
                path = path.substring(colonIndex + 1);
            } else {
                path = path.replaceFirst("^https?://[^/]+/", "");
            }
            path = path.replaceFirst("\\.git$", "");
            String[] parts = path.split("/");
            if (parts.length < 2) {
                return new RepositoryCoordinates("", "", "");
            }
            return new RepositoryCoordinates(parts[parts.length - 2], parts[parts.length - 1], path);
        }
    }
}
