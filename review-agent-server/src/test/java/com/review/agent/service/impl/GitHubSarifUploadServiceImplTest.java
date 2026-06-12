package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubSarifUploadRequest;
import com.review.agent.domain.dto.GitHubSarifUploadResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubSarifUploadClient;
import com.review.agent.infrastructure.ci.GitHubSarifUploadRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubSarifUploadRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubSarifUploadServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final RecordingClient client = new RecordingClient();
    private final GitHubSarifUploadServiceImpl service = new GitHubSarifUploadServiceImpl(
            repository,
            new GitHubSarifUploadRequestFactory(),
            client);

    @Test
    void uploadsSarifWhenConfigIsReady() {
        repository.config = config(true);

        GitHubSarifUploadResultVO result = service.upload(request());

        assertEquals("UPLOADED", result.getStatus());
        assertEquals("https://api.github.com/repos/zhu930824/review-agent/code-scanning/sarifs", result.getRequestUrl());
        assertEquals("abc123", client.request.body().get("commit_sha"));
        assertEquals("refs/heads/main", client.request.body().get("ref"));
    }

    @Test
    void skipsUploadWhenSarifUploadIsDisabled() {
        repository.config = config(false);

        GitHubSarifUploadResultVO result = service.upload(request());

        assertEquals("SKIPPED", result.getStatus());
        assertEquals("github sarif upload config is not ready", result.getMessage());
        assertEquals(null, client.request);
    }

    private GitHubSarifUploadRequest request() {
        GitHubSarifUploadRequest request = new GitHubSarifUploadRequest();
        request.setCommitSha("abc123");
        request.setRef("refs/heads/main");
        request.setSarif("{\"version\":\"2.1.0\",\"runs\":[]}");
        return request;
    }

    private CiStatusConfig config(boolean enabled) {
        CiStatusConfig config = new CiStatusConfig();
        config.setProvider("GITHUB");
        config.setRepoOwner("zhu930824");
        config.setRepoName("review-agent");
        config.setApiToken("ghp_secret");
        config.setSarifUploadEnabled(enabled);
        return config;
    }

    private static class FakeRepository implements GitHubSarifUploadRepository {
        private CiStatusConfig config;

        @Override
        public Optional<CiStatusConfig> findConfig(String connectorKey) {
            return Optional.ofNullable(config);
        }
    }

    private static class RecordingClient implements GitHubSarifUploadClient {
        private com.review.agent.infrastructure.ci.GitHubSarifUploadRequest request;

        @Override
        public void upload(com.review.agent.infrastructure.ci.GitHubSarifUploadRequest request) {
            this.request = request;
        }
    }
}
