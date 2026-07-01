package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitHubSarifUploadRequest;
import com.review.agent.domain.dto.GitHubSarifUploadResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.ci.GitHubSarifUploadClient;
import com.review.agent.infrastructure.ci.GitHubSarifUploadRequestFactory;
import com.review.agent.infrastructure.persistence.GitHubSarifUploadRepository;
import com.review.agent.service.GitHubSarifUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSarifUploadServiceImpl implements GitHubSarifUploadService {

    private static final String CONNECTOR_KEY = "github-checks";
    private static final String ACTION_TYPE = "SARIF_UPLOAD";

    private final GitHubSarifUploadRepository repository;
    private final GitHubSarifUploadRequestFactory requestFactory;
    private final GitHubSarifUploadClient uploadClient;

    @Override
    public GitHubSarifUploadResultVO upload(GitHubSarifUploadRequest request) {
        CiStatusConfig config = repository.findConfig(CONNECTOR_KEY).orElse(null);
        if (!isReady(config)) {
            safeRecord("SKIPPED", request == null ? null : request.getCommitSha(), null, "github sarif upload config is not ready");
            return result("SKIPPED", "github sarif upload config is not ready", null);
        }

        com.review.agent.infrastructure.ci.GitHubSarifUploadRequest uploadRequest = requestFactory.build(
                config,
                request.getCommitSha(),
                request.getRef(),
                request.getSarif());
        try {
            uploadClient.upload(uploadRequest);
            safeRecord("UPLOADED", request.getCommitSha(), uploadRequest.url(), null);
            return result("UPLOADED", "github sarif upload accepted", uploadRequest.url());
        } catch (Exception e) {
            safeRecord("FAILED", request.getCommitSha(), uploadRequest.url(), e.getMessage());
            return result("FAILED", e.getMessage(), uploadRequest.url());
        }
    }

    private boolean isReady(CiStatusConfig config) {
        return config != null
                && Boolean.TRUE.equals(config.getSarifUploadEnabled())
                && "GITHUB".equalsIgnoreCase(config.getProvider())
                && hasText(config.getRepoOwner())
                && hasText(config.getRepoName())
                && hasText(config.getApiToken());
    }

    private GitHubSarifUploadResultVO result(String status, String message, String requestUrl) {
        GitHubSarifUploadResultVO result = new GitHubSarifUploadResultVO();
        result.setStatus(status);
        result.setMessage(message);
        result.setRequestUrl(requestUrl);
        return result;
    }

    private void safeRecord(String status, String commitSha, String requestUrl, String errorMessage) {
        try {
            repository.recordAction(ACTION_TYPE, status, commitSha, requestUrl, errorMessage);
        } catch (Exception e) {
            log.warn("[IntegrationAction] failed to record sarif upload status={}", status, e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
