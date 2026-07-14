ALTER TABLE integration_webhook_review_trigger
    ADD COLUMN external_event_id VARCHAR(200) NULL COMMENT 'Provider event, merge request or build identifier',
    ADD COLUMN external_event_url VARCHAR(1000) NULL COMMENT 'Provider event or build URL',
    ADD COLUMN commit_sha VARCHAR(100) NULL COMMENT 'Source revision used for idempotency';

ALTER TABLE integration_webhook_review_trigger
    COMMENT = 'Idempotent GitLab and Jenkins review triggers';
