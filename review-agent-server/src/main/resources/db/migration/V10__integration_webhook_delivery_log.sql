CREATE TABLE IF NOT EXISTS integration_webhook_delivery_log (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    connector_key   VARCHAR(100)  NOT NULL COMMENT 'Stable connector key',
    provider        VARCHAR(50)   NOT NULL COMMENT 'Webhook provider',
    delivery_id     VARCHAR(200)  NOT NULL COMMENT 'Provider delivery id for idempotency',
    event_type      VARCHAR(100)  NOT NULL COMMENT 'Provider event type',
    delivery_status VARCHAR(30)   NOT NULL COMMENT 'ACCEPTED/REJECTED/DUPLICATE',
    signature       VARCHAR(200)  DEFAULT NULL COMMENT 'Received signature header',
    payload_digest  VARCHAR(100)  DEFAULT NULL COMMENT 'SHA-256 digest of received payload',
    error_message   VARCHAR(1000) DEFAULT NULL COMMENT 'Failure reason',
    received_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Received time',
    processed_at    DATETIME      DEFAULT NULL COMMENT 'Processed time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_webhook_delivery (connector_key, delivery_id),
    INDEX idx_webhook_status (delivery_status),
    INDEX idx_webhook_event_type (event_type),
    INDEX idx_webhook_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration webhook delivery log';
