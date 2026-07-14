package com.review.agent.service.impl;

import com.review.agent.domain.dto.CredentialRotationRequest;
import com.review.agent.domain.dto.CredentialRotationResultVO;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.infrastructure.security.CredentialEncryptionManager;
import com.review.agent.service.CredentialRotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialRotationServiceImpl implements CredentialRotationService {

    private static final String CONFIRMATION = "ROTATE CREDENTIALS";

    private final JdbcTemplate jdbcTemplate;
    private final CredentialEncryptionManager encryptionManager;
    private final IntegrationActionLogRepository actionLogRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public CredentialRotationResultVO rotate(CredentialRotationRequest request) {
        if (request == null || !CONFIRMATION.equals(request.getConfirmation())) {
            throw new IllegalArgumentException("Credential rotation confirmation must be ROTATE CREDENTIALS");
        }

        try {
            Integer rotated = new TransactionTemplate(transactionManager).execute(status -> rotateInTransaction());
            int rotatedCount = rotated == null ? 0 : rotated;
            safeRecord("ROTATED", rotatedCount + " credential(s)", null);
            return result(rotatedCount);
        } catch (RuntimeException ex) {
            safeRecord("FAILED", "credential rotation", safeMessage(ex));
            throw ex;
        }
    }

    private int rotateInTransaction() {
        List<CredentialUpdate> updates = new ArrayList<>();
        collectUpdates("integration_ci_config", "api_token", updates);
        collectUpdates("integration_ci_config", "webhook_secret", updates);
        collectUpdates("project_gitlab_config", "gitlab_token", updates);

        for (CredentialUpdate update : updates) {
            jdbcTemplate.update(
                    "UPDATE " + update.table + " SET " + update.column + " = ? WHERE id = ?",
                    update.rotatedValue,
                    update.id);
        }
        return updates.size();
    }

    private void collectUpdates(String table, String column, List<CredentialUpdate> updates) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, " + column + " AS credential FROM " + table
                        + " WHERE " + column + " IS NOT NULL AND " + column + " <> ''");
        for (Map<String, Object> row : rows) {
            String storedValue = String.valueOf(row.get("credential"));
            CredentialEncryptionManager.CredentialKeyState state = encryptionManager.inspect(storedValue);
            if (state == CredentialEncryptionManager.CredentialKeyState.ACTIVE) {
                continue;
            }
            if (state == CredentialEncryptionManager.CredentialKeyState.UNREADABLE) {
                throw new IllegalStateException(
                        "Credential cannot be decrypted in " + table + "." + column + " for id " + row.get("id"));
            }
            updates.add(new CredentialUpdate(
                    table,
                    column,
                    row.get("id"),
                    encryptionManager.reencrypt(storedValue)));
        }
    }

    private CredentialRotationResultVO result(int rotatedCount) {
        CredentialRotationResultVO result = new CredentialRotationResultVO();
        result.setStatus("ROTATED");
        result.setRotatedCredentialCount(rotatedCount);
        result.setMessage(rotatedCount == 0
                ? "All integration credentials already use the active key."
                : "Integration credentials were re-encrypted with the active key.");
        result.setRotatedAt(LocalDateTime.now());
        return result;
    }

    private void safeRecord(String status, String targetKey, String errorMessage) {
        try {
            LocalDateTime now = LocalDateTime.now();
            IntegrationActionLog logEntry = new IntegrationActionLog();
            logEntry.setConnectorKey("credential-security");
            logEntry.setProvider("PLATFORM");
            logEntry.setActionType("CREDENTIAL_ROTATION");
            logEntry.setActionStatus(status);
            logEntry.setTargetKey(targetKey);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setCreatedAt(now);
            logEntry.setUpdatedAt(now);
            actionLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to record credential rotation action", ex);
        }
    }

    private String safeMessage(Throwable ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }

    private record CredentialUpdate(String table, String column, Object id, String rotatedValue) {
    }
}
