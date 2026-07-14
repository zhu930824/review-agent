package com.review.agent.service.impl;

import com.review.agent.domain.dto.GitLabReviewTriggerHealthVO;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.service.GitLabReviewTriggerHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitLabReviewTriggerHealthServiceImpl implements GitLabReviewTriggerHealthService {

    private static final String CONNECTOR_KEY = "gitlab-merge-request";

    private final IntegrationWebhookReviewTriggerRepository repository;

    @Override
    public GitLabReviewTriggerHealthVO getHealth() {
        GitLabReviewTriggerHealthVO health = new GitLabReviewTriggerHealthVO();
        health.setProcessingCount(repository.countByStatus(CONNECTOR_KEY, "PROCESSING"));
        health.setFailedCount(repository.countByStatus(CONNECTOR_KEY, "FAILED"));
        health.setExhaustedCount(repository.countByStatus(CONNECTOR_KEY, "EXHAUSTED"));
        health.setProcessedCount(repository.countByStatus(CONNECTOR_KEY, "PROCESSED"));
        repository.findOldestPending(CONNECTOR_KEY).ifPresent(item -> health.setOldestPendingAt(item.getUpdatedAt()));
        health.setHealthStatus(status(health));
        health.setSummary(summary(health));
        return health;
    }

    private String status(GitLabReviewTriggerHealthVO health) {
        if (health.getExhaustedCount() > 0) {
            return "UNHEALTHY";
        }
        if (health.getFailedCount() > 0) {
            return "DEGRADED";
        }
        return "HEALTHY";
    }

    private String summary(GitLabReviewTriggerHealthVO health) {
        return health.getProcessingCount() + " processing, "
                + health.getFailedCount() + " waiting for retry, "
                + health.getExhaustedCount() + " exhausted, "
                + health.getProcessedCount() + " processed.";
    }
}
