package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GitHubPrSummaryCommentRequestFactory {

    public GitHubPrSummaryCommentRequest build(CiStatusConfig config, Integer pullNumber, String bodyText) {
        String url = "https://api.github.com/repos/%s/%s/issues/%s/comments"
                .formatted(config.getRepoOwner(), config.getRepoName(), pullNumber);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiToken());
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2022-11-28");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("body", bodyText);

        return new GitHubPrSummaryCommentRequest(url, headers, body);
    }
}
