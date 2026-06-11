CREATE TABLE IF NOT EXISTS integration_ci_writeback_log (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    connector_key    VARCHAR(100)  NOT NULL COMMENT 'Stable connector key',
    provider         VARCHAR(50)   NOT NULL COMMENT 'CI provider',
    review_id        BIGINT        DEFAULT NULL COMMENT 'Review id',
    commit_sha       VARCHAR(100)  DEFAULT NULL COMMENT 'Commit SHA used for writeback',
    state            VARCHAR(50)   DEFAULT NULL COMMENT 'Provider state, e.g. pending/success/failure',
    writeback_status VARCHAR(30)   NOT NULL COMMENT 'SUCCESS/FAILED/SKIPPED',
    request_url      VARCHAR(1000) DEFAULT NULL COMMENT 'Provider request URL',
    error_message    VARCHAR(2000) DEFAULT NULL COMMENT 'Failure or skipped reason',
    retry_count      INT           NOT NULL DEFAULT 0 COMMENT 'Retry attempts',
    next_retry_at    DATETIME      DEFAULT NULL COMMENT 'Next retry time',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    INDEX idx_ci_writeback_review (review_id),
    INDEX idx_ci_writeback_status (writeback_status),
    INDEX idx_ci_writeback_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CI status writeback log';
