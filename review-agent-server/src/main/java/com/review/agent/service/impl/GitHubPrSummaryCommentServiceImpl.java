package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentClient;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubPrSummaryCommentRepository;
import com.review.agent.service.GitHubPrSummaryCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubPrSummaryCommentServiceImpl implements GitHubPrSummaryCommentService {

    private static final String CONNECTOR_KEY = "github-checks";

    private final GitHubPrSummaryCommentRepository repository;
    private final GitHubPrSummaryCommentRequestFactory requestFactory;
    private final GitHubPrSummaryCommentClient client;

    @Override
    public GitHubPrSummaryCommentResultVO comment(GitHubPrSummaryCommentRequest request) {
        CiStatusConfig config = repository.findConfig(CONNECTOR_KEY).orElse(null);
        if (!isReady(config) || request == null || request.getPullNumber() == null || !hasText(request.getBody())) {
            return result("SKIPPED", "github pr summary config is not ready", null);
        }

        com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest commentRequest = requestFactory.build(
                config,
                request.getPullNumber(),
                request.getBody());
        client.post(commentRequest);
        return result("POSTED", "github pr summary comment posted", commentRequest.url());
    }

    private boolean isReady(CiStatusConfig config) {
        return config != null
                && "GITHUB".equalsIgnoreCase(config.getProvider())
                && hasText(config.getRepoOwner())
                && hasText(config.getRepoName())
                && hasText(config.getApiToken());
    }

    private GitHubPrSummaryCommentResultVO result(String status, String message, String requestUrl) {
        GitHubPrSummaryCommentResultVO result = new GitHubPrSummaryCommentResultVO();
        result.setStatus(status);
        result.setMessage(message);
        result.setRequestUrl(requestUrl);
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
