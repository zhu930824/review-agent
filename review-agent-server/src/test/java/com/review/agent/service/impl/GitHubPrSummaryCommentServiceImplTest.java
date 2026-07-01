package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentClient;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubPrSummaryCommentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

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
        assertEquals("POSTED", repository.records.get(0).status);
        assertEquals("17", repository.records.get(0).targetKey);
        assertEquals("https://api.github.com/repos/zhu930824/review-agent/issues/17/comments", repository.records.get(0).requestUrl);
    }

    @Test
    void skipsPrSummaryCommentWhenConfigIsMissing() {
        GitHubPrSummaryCommentResultVO result = service.comment(request());

        assertEquals("SKIPPED", result.getStatus());
        assertEquals("github pr summary config is not ready", result.getMessage());
        assertEquals(null, client.request);
        assertEquals("SKIPPED", repository.records.get(0).status);
        assertEquals("github pr summary config is not ready", repository.records.get(0).errorMessage);
    }

    @Test
    void recordsFailureWhenGitHubPrSummaryCommentFails() {
        repository.config = config();
        client.failure = new RuntimeException("github unavailable");

        GitHubPrSummaryCommentResultVO result = service.comment(request());

        assertEquals("FAILED", result.getStatus());
        assertEquals("github unavailable", result.getMessage());
        assertEquals("FAILED", repository.records.get(0).status);
        assertEquals("github unavailable", repository.records.get(0).errorMessage);
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
        private final List<Record> records = new ArrayList<>();

        @Override
        public Optional<CiStatusConfig> findConfig(String connectorKey) {
            return Optional.ofNullable(config);
        }

        public void recordAction(String actionType, String status, String targetKey, String requestUrl, String errorMessage) {
            records.add(new Record(actionType, status, targetKey, requestUrl, errorMessage));
        }
    }

    private static class RecordingClient implements GitHubPrSummaryCommentClient {
        private com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest request;
        private RuntimeException failure;

        @Override
        public void post(com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest request) {
            if (failure != null) {
                throw failure;
            }
            this.request = request;
        }
    }

    private record Record(String actionType, String status, String targetKey, String requestUrl, String errorMessage) {
    }
}
