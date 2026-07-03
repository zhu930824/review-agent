CREATE TABLE IF NOT EXISTS governance_rule_pack_version (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rule_pack_key VARCHAR(100) NOT NULL,
    source_change_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    version_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    title VARCHAR(500) NOT NULL,
    controls_snapshot TEXT DEFAULT NULL,
    rationale VARCHAR(1000) DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT 'governance',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule_pack_version_change (source_change_id),
    INDEX idx_rule_pack_version_pack (rule_pack_key),
    INDEX idx_rule_pack_version_status (version_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治理规则包版本快照';
