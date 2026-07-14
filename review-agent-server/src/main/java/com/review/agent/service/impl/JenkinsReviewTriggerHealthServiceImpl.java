package com.review.agent.service.impl;

import com.review.agent.domain.dto.JenkinsReviewTriggerHealthVO;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.service.JenkinsReviewTriggerHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JenkinsReviewTriggerHealthServiceImpl implements JenkinsReviewTriggerHealthService {

    private static final String DEFAULT_CONNECTOR_KEY = "jenkins-pipeline";

    private final IntegrationWebhookReviewTriggerRepository repository;

    @Override
    public JenkinsReviewTriggerHealthVO getHealth(String connectorKey) {
        String key = normalizeConnectorKey(connectorKey);
        JenkinsReviewTriggerHealthVO health = new JenkinsReviewTriggerHealthVO();
        health.setProcessingCount(repository.countByStatus(key, "PROCESSING"));
        health.setFailedCount(repository.countByStatus(key, "FAILED"));
        health.setExhaustedCount(repository.countByStatus(key, "EXHAUSTED"));
        health.setProcessedCount(repository.countByStatus(key, "PROCESSED"));
        repository.findOldestPending(key).ifPresent(item -> health.setOldestPendingAt(item.getUpdatedAt()));
        health.setHealthStatus(status(health));
        health.setSummary(health.getProcessingCount() + " processing, "
                + health.getFailedCount() + " waiting for retry, "
                + health.getExhaustedCount() + " exhausted, "
                + health.getProcessedCount() + " processed.");
        return health;
    }

    private String status(JenkinsReviewTriggerHealthVO health) {
        if (health.getExhaustedCount() > 0) {
            return "UNHEALTHY";
        }
        if (health.getFailedCount() > 0) {
            return "DEGRADED";
        }
        return "HEALTHY";
    }

    private String normalizeConnectorKey(String connectorKey) {
        String key = connectorKey == null || connectorKey.isBlank()
                ? DEFAULT_CONNECTOR_KEY
                : connectorKey.trim().toLowerCase();
        if (!key.equals(DEFAULT_CONNECTOR_KEY) && !key.startsWith(DEFAULT_CONNECTOR_KEY + ":")) {
            throw new IllegalArgumentException("Invalid Jenkins connector key: " + key);
        }
        return key;
    }
}
