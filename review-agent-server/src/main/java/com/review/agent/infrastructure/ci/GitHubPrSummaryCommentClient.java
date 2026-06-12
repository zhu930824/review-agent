package com.review.agent.infrastructure.ci;

public interface GitHubPrSummaryCommentClient {

    void post(GitHubPrSummaryCommentRequest request);
}
