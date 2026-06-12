package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubPrSummaryCommentRequestFactoryTest {

    private final GitHubPrSummaryCommentRequestFactory factory = new GitHubPrSummaryCommentRequestFactory();

    @Test
    void buildsGitHubIssueCommentRequestForPullRequestSummary() {
        GitHubPrSummaryCommentRequest request = factory.build(config(), 17, "Review Agent summary");

        assertEquals("https://api.github.com/repos/zhu930824/review-agent/issues/17/comments", request.url());
        assertEquals("Bearer ghp_secret", request.headers().get("Authorization"));
        assertEquals("application/vnd.github+json", request.headers().get("Accept"));
        assertEquals("2022-11-28", request.headers().get("X-GitHub-Api-Version"));
        assertEquals("Review Agent summary", request.body().get("body"));
    }

    private CiStatusConfig config() {
        CiStatusConfig config = new CiStatusConfig();
        config.setProvider("GITHUB");
        config.setRepoOwner("zhu930824");
        config.setRepoName("review-agent");
        config.setApiToken("ghp_secret");
        return config;
    }
}
