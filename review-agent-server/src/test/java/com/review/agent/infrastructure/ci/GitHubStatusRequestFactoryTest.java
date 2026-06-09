package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubStatusRequestFactoryTest {

    private final GitHubStatusRequestFactory factory = new GitHubStatusRequestFactory();

    @Test
    void buildsGitHubStatusRequestWithRepositoryBindingAndAuth() {
        CiStatusConfig config = config();

        GitHubStatusRequest request = factory.build(config, "abc123", "success", "Pre-PR review passed", 42L);

        assertEquals("https://api.github.com/repos/zhu930824/review-agent/statuses/abc123", request.url());
        assertEquals("Bearer ghp_secret", request.headers().get("Authorization"));
        assertEquals("application/vnd.github+json", request.headers().get("Accept"));
        assertEquals("success", request.body().get("state"));
        assertEquals("Review Agent", request.body().get("context"));
        assertEquals("Pre-PR review passed", request.body().get("description"));
        assertEquals("/reviews/42", request.body().get("target_url"));
    }

    @Test
    void capsGitHubDescriptionToStatusApiLimit() {
        CiStatusConfig config = config();
        String longDescription = "x".repeat(200);

        GitHubStatusRequest request = factory.build(config, "abc123", "failure", longDescription, 42L);

        assertEquals("failure", request.body().get("state"));
        assertTrue(((String) request.body().get("description")).length() <= 140);
    }

    private CiStatusConfig config() {
        CiStatusConfig config = new CiStatusConfig();
        config.setProvider("GITHUB");
        config.setRepoOwner("zhu930824");
        config.setRepoName("review-agent");
        config.setStatusContext("Review Agent");
        config.setApiToken("ghp_secret");
        return config;
    }
}
