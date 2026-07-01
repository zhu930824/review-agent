CREATE TABLE IF NOT EXISTS integration_action_log (
    id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    connector_key VARCHAR(100)  NOT NULL COMMENT 'Stable connector key',
    provider      VARCHAR(50)   NOT NULL COMMENT 'Integration provider',
    action_type   VARCHAR(80)   NOT NULL COMMENT 'Action type, e.g. SARIF_UPLOAD/PR_SUMMARY_COMMENT',
    action_status VARCHAR(30)   NOT NULL COMMENT 'UPLOADED/POSTED/SKIPPED/FAILED',
    target_key    VARCHAR(200)  DEFAULT NULL COMMENT 'Provider target key, e.g. pull request number',
    commit_sha    VARCHAR(100)  DEFAULT NULL COMMENT 'Commit SHA when action targets code scanning or status',
    request_url   VARCHAR(1000) DEFAULT NULL COMMENT 'Provider request URL',
    error_message VARCHAR(2000) DEFAULT NULL COMMENT 'Failure or skipped reason',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    INDEX idx_integration_action_type (action_type),
    INDEX idx_integration_action_status (action_status),
    INDEX idx_integration_action_created_at (created_at),
    INDEX idx_integration_action_target (target_key),
    INDEX idx_integration_action_commit (commit_sha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration external action log';
