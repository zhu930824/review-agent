package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentClient;
import com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubPrSummaryCommentRepository;
import com.review.agent.service.GitHubPrSummaryCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubPrSummaryCommentServiceImpl implements GitHubPrSummaryCommentService {

    private static final String CONNECTOR_KEY = "github-checks";
    private static final String ACTION_TYPE = "PR_SUMMARY_COMMENT";

    private final GitHubPrSummaryCommentRepository repository;
    private final GitHubPrSummaryCommentRequestFactory requestFactory;
    private final GitHubPrSummaryCommentClient client;

    @Override
    public GitHubPrSummaryCommentResultVO comment(GitHubPrSummaryCommentRequest request) {
        CiStatusConfig config = repository.findConfig(CONNECTOR_KEY).orElse(null);
        if (!isReady(config) || request == null || request.getPullNumber() == null || !hasText(request.getBody())) {
            safeRecord("SKIPPED", request == null || request.getPullNumber() == null ? null : String.valueOf(request.getPullNumber()), null, "github pr summary config is not ready");
            return result("SKIPPED", "github pr summary config is not ready", null);
        }

        com.review.agent.infrastructure.ci.GitHubPrSummaryCommentRequest commentRequest = requestFactory.build(
                config,
                request.getPullNumber(),
                request.getBody());
        try {
            client.post(commentRequest);
            safeRecord("POSTED", String.valueOf(request.getPullNumber()), commentRequest.url(), null);
            return result("POSTED", "github pr summary comment posted", commentRequest.url());
        } catch (Exception e) {
            safeRecord("FAILED", String.valueOf(request.getPullNumber()), commentRequest.url(), e.getMessage());
            return result("FAILED", e.getMessage(), commentRequest.url());
        }
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

    private void safeRecord(String status, String targetKey, String requestUrl, String errorMessage) {
        try {
            repository.recordAction(ACTION_TYPE, status, targetKey, requestUrl, errorMessage);
        } catch (Exception e) {
            log.warn("[IntegrationAction] failed to record pr summary status={}", status, e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
