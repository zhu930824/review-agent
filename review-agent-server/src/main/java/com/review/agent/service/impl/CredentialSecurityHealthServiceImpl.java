package com.review.agent.service.impl;

import com.review.agent.domain.dto.CredentialSecurityHealthVO;
import com.review.agent.infrastructure.security.CredentialEncryptionManager;
import com.review.agent.service.CredentialSecurityHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CredentialSecurityHealthServiceImpl implements CredentialSecurityHealthService {

    private final JdbcTemplate jdbcTemplate;
    private final CredentialEncryptionManager encryptionManager;

    @Override
    public CredentialSecurityHealthVO getHealth() {
        CredentialCounts counts = inspectCredentials();
        CredentialSecurityHealthVO health = new CredentialSecurityHealthVO();
        health.setEncryptionEnabled(encryptionManager.isConfigured());
        health.setDevelopmentKey(encryptionManager.isDevelopmentKey());
        health.setPreviousKeysConfigured(encryptionManager.hasPreviousKeys());
        health.setRotationRequired(counts.previous > 0 || counts.plaintext > 0);
        health.setEncryptedCredentialCount(counts.active + counts.previous + counts.unreadable);
        health.setPlaintextCredentialCount(counts.plaintext);
        health.setActiveKeyCredentialCount(counts.active);
        health.setPreviousKeyCredentialCount(counts.previous);
        health.setUnreadableCredentialCount(counts.unreadable);
        if (counts.unreadable > 0) {
            health.setStatus("KEY_MISMATCH");
            health.setRecommendation(
                    "Some credentials cannot be decrypted. Add their old key to REVIEW_AGENT_CREDENTIAL_PREVIOUS_KEYS before rotation.");
        } else if (counts.plaintext > 0) {
            health.setStatus("MIGRATION_REQUIRED");
            health.setRecommendation("Restart the service to migrate remaining plaintext integration credentials.");
        } else if (counts.previous > 0) {
            health.setStatus("ROTATION_REQUIRED");
            health.setRecommendation(
                    "Credentials still use a previous key. Run credential rotation, then remove REVIEW_AGENT_CREDENTIAL_PREVIOUS_KEYS.");
        } else if (encryptionManager.isDevelopmentKey()) {
            health.setStatus("DEVELOPMENT_KEY");
            health.setRecommendation("Set REVIEW_AGENT_CREDENTIAL_KEY to a stable enterprise secret before production.");
        } else {
            health.setStatus("SECURE");
            health.setRecommendation("Integration credentials are encrypted with the configured enterprise key.");
        }
        return health;
    }

    private CredentialCounts inspectCredentials() {
        CredentialCounts counts = new CredentialCounts();
        inspect("integration_ci_config", "api_token", counts);
        inspect("integration_ci_config", "webhook_secret", counts);
        inspect("project_gitlab_config", "gitlab_token", counts);
        return counts;
    }

    private void inspect(String table, String column, CredentialCounts counts) {
        List<String> values = jdbcTemplate.queryForList(
                "SELECT " + column + " FROM " + table
                        + " WHERE " + column + " IS NOT NULL AND " + column + " <> ''",
                String.class);
        for (String value : values) {
            switch (encryptionManager.inspect(value)) {
                case ACTIVE -> counts.active++;
                case PREVIOUS -> counts.previous++;
                case PLAINTEXT -> counts.plaintext++;
                case UNREADABLE -> counts.unreadable++;
            }
        }
    }

    private static class CredentialCounts {
        private long active;
        private long previous;
        private long plaintext;
        private long unreadable;
    }
}
