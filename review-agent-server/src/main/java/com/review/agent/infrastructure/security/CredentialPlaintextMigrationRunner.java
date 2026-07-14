package com.review.agent.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CredentialPlaintextMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CredentialEncryptionManager encryptionManager;

    @Override
    public void run(ApplicationArguments args) {
        int migrated = 0;
        migrated += migrate("integration_ci_config", "api_token");
        migrated += migrate("integration_ci_config", "webhook_secret");
        migrated += migrate("project_gitlab_config", "gitlab_token");
        if (migrated > 0) {
            log.info("Encrypted {} legacy integration credential values", migrated);
        }
    }

    private int migrate(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, " + column + " AS credential FROM " + table
                        + " WHERE " + column + " IS NOT NULL AND " + column + " <> ''"
                        + " AND " + column + " NOT LIKE 'enc:v1:%'");
        int migrated = 0;
        for (Map<String, Object> row : rows) {
            Object credential = row.get("credential");
            if (credential == null) {
                continue;
            }
            migrated += jdbcTemplate.update(
                    "UPDATE " + table + " SET " + column + " = ? WHERE id = ?",
                    encryptionManager.encrypt(String.valueOf(credential)),
                    row.get("id"));
        }
        return migrated;
    }
}
