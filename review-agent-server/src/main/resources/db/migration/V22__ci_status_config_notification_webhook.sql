ALTER TABLE integration_ci_config
    ADD COLUMN notification_webhook_url VARCHAR(1000) DEFAULT NULL COMMENT 'Optional webhook endpoint for CI health notifications';
