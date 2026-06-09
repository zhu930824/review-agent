package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GitHubStatusRequestFactory {

    private static final int DESCRIPTION_LIMIT = 140;

    public GitHubStatusRequest build(
            CiStatusConfig config,
            String commitSha,
            String state,
            String description,
            Long reviewId) {
        String url = "https://api.github.com/repos/%s/%s/statuses/%s"
                .formatted(config.getRepoOwner(), config.getRepoName(), commitSha);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiToken());
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2022-11-28");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("state", state);
        body.put("context", config.getStatusContext());
        body.put("description", truncate(description));
        body.put("target_url", "/reviews/" + reviewId);

        return new GitHubStatusRequest(url, headers, body);
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
}
