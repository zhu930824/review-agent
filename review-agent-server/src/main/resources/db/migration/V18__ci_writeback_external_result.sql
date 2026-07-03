ALTER TABLE integration_ci_writeback_log
    ADD COLUMN external_queue_url VARCHAR(1000) DEFAULT NULL COMMENT 'External CI queue URL',
    ADD COLUMN external_build_url VARCHAR(1000) DEFAULT NULL COMMENT 'External CI build URL',
    ADD COLUMN external_build_number VARCHAR(100) DEFAULT NULL COMMENT 'External CI build number',
    ADD COLUMN external_build_result VARCHAR(100) DEFAULT NULL COMMENT 'External CI build result',
    ADD COLUMN external_result_updated_at DATETIME DEFAULT NULL COMMENT 'External CI result update time';
