package com.review.agent.infrastructure.ci;

public interface GitHubSarifUploadClient {

    void upload(GitHubSarifUploadRequest request);
}
