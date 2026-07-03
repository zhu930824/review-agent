package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitLabStatusRequestFactoryTest {

    private final GitLabStatusRequestFactory factory = new GitLabStatusRequestFactory();

    @Test
    void buildsCommitStatusRequestFromProjectUrlAndNamespace() {
        CiStatusConfig config = new CiStatusConfig();
        config.setRepoUrl("https://gitlab.example.com/platform/team/review-agent");
        config.setRepoOwner("platform/team");
        config.setRepoName("review-agent");
        config.setStatusContext("review-agent/gate");
        config.setApiToken("gl-token");

        CiProviderStatusRequest request = factory.build(config, "abc123", "failure", "blocked by review", 42L);

        assertEquals("https://gitlab.example.com/api/v4/projects/platform%2Fteam%2Freview-agent/statuses/abc123", request.url());
        assertEquals("gl-token", request.headers().get("PRIVATE-TOKEN"));
        assertEquals("failed", request.body().get("state"));
        assertEquals("review-agent/gate", request.body().get("name"));
        assertEquals("/reviews/42", request.body().get("target_url"));
    }
}
