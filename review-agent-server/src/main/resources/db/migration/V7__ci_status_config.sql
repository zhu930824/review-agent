CREATE TABLE IF NOT EXISTS integration_ci_config (
    id                         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    connector_key              VARCHAR(100)  NOT NULL COMMENT 'Stable connector key, for example github-checks',
    provider                   VARCHAR(50)   NOT NULL COMMENT 'CI provider, for example GITHUB',
    repo_owner                 VARCHAR(200)  NOT NULL COMMENT 'Repository owner or organization',
    repo_name                  VARCHAR(200)  NOT NULL COMMENT 'Repository name',
    repo_url                   VARCHAR(500)  DEFAULT NULL COMMENT 'Repository URL',
    default_branch             VARCHAR(100)  DEFAULT NULL COMMENT 'Default branch',
    status_context             VARCHAR(200)  NOT NULL COMMENT 'Status check context name',
    checks_enabled             TINYINT       NOT NULL DEFAULT 1 COMMENT 'Whether checks/status writeback is enabled',
    sarif_upload_enabled       TINYINT       NOT NULL DEFAULT 0 COMMENT 'Whether SARIF upload is enabled',
    api_token                  VARCHAR(1000) DEFAULT NULL COMMENT 'Encrypted or protected API token',
    webhook_secret             VARCHAR(1000) DEFAULT NULL COMMENT 'Encrypted or protected webhook secret',
    created_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_integration_ci_config_connector (connector_key),
    INDEX idx_integration_ci_config_repo (repo_owner, repo_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CI status writeback configuration';
