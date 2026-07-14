ALTER TABLE integration_webhook_delivery_log
    ADD COLUMN trigger_status VARCHAR(20) NULL COMMENT 'MR review trigger status',
    ADD COLUMN trigger_key VARCHAR(500) NULL COMMENT 'Stable GitLab MR revision trigger key',
    ADD COLUMN trigger_review_id BIGINT NULL COMMENT 'Review created from the webhook delivery',
    ADD COLUMN trigger_message VARCHAR(1000) NULL COMMENT 'MR review trigger result or error message',
    ADD COLUMN trigger_retry_count INT NULL COMMENT 'Automatic review trigger retry attempts',
    ADD COLUMN trigger_next_retry_at DATETIME NULL COMMENT 'Next automatic review trigger retry time';

CREATE TABLE integration_webhook_review_trigger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    connector_key VARCHAR(100) NOT NULL,
    trigger_key VARCHAR(500) NOT NULL,
    trigger_status VARCHAR(20) NOT NULL,
    project_id BIGINT NOT NULL,
    source_branch VARCHAR(255) NOT NULL,
    target_branch VARCHAR(255) NOT NULL,
    merge_request_iid VARCHAR(100) NULL,
    merge_request_url VARCHAR(1000) NULL,
    review_id BIGINT NULL,
    message VARCHAR(1000) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_webhook_review_trigger (connector_key, trigger_key),
    KEY idx_webhook_review_trigger_status (trigger_status, next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Idempotent GitLab MR revision review triggers';

ALTER TABLE project_gitlab_config
    MODIFY COLUMN gitlab_token VARCHAR(1000) NOT NULL COMMENT 'AES-GCM encrypted GitLab Personal Access Token',
    ADD COLUMN auto_review_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Automatically review matching GitLab merge requests',
    ADD COLUMN review_drafts TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Allow Draft or WIP merge request reviews',
    ADD COLUMN publish_summary_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Automatically publish completed review summary to GitLab MR',
    ADD COLUMN target_branch_pattern VARCHAR(500) NULL COMMENT 'Comma or newline separated target branch patterns';
