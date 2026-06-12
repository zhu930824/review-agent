package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentClient;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubPrSummaryCommentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubPrSummaryCommentServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final RecordingClient client = new RecordingClient();
    private final GitHubPrSummaryCommentServiceImpl service = new GitHubPrSummaryCommentServiceImpl(
            repository,
            new GitHubPrSummaryCommentRequestFactory(),
            client);

    @Test
    void postsPrSummaryCommentWhenConfigIsReady() {
        repository.config = config();

        GitHubPrSummaryCommentResultVO result = service.comment(request());

        assertEquals("POSTED", result.getStatus());
        assertEquals("https://api.github.com/repos/zhu930824/review-agent/issues/17/comments", result.getRequestUrl());
        assertEquals("Review Agent summary", client.request.body().get("body"));
    }

    @Test
    void skipsPrSummaryCommentWhenConfigIsMissing() {
        GitHubPrSummaryCommentResultVO result = service.comment(request());

        assertEquals("SKIPPED", result.getStatus());
        assertEquals("github pr summary config is not ready", result.getMessage());
        assertEquals(null, client.request);
    }

    private GitHubPrSummaryCommentRequest request() {
        GitHubPrSummaryCommentRequest request = new GitHubPrSummaryCommentRequest();
        request.setPullNumber(17);
        request.setBody("Review Agent summary");
        return request;
    }

    private CiStatusConfig config() {
        CiStatusConfig config = new CiStatusConfig();
        config.setProvider("GITHUB");
        config.setRepoOwner("zhu930824");
        config.setRepoName("review-agent");
        config.setApiToken("ghp_secret");
        return config;
    }

    private static class FakeRepository implements GitHubPrSummaryCommentRepository {
        private CiStatusConfig config;

        @Override
        public Optional<CiStatusConfig> findConfig(String connectorKey) {
            return Optional.ofNullable(config);
        }
    }

    private static class RecordingClient implements GitHubPrSummaryCommentClient {
        private com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest request;

        @Override
        public void post(com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest request) {
            this.request = request;
        }
    }
}
