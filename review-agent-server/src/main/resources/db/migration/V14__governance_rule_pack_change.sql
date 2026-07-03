CREATE TABLE IF NOT EXISTS governance_rule_pack_change (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    rule_pack_key   VARCHAR(100)  NOT NULL DEFAULT 'team-rule-memory' COMMENT '目标规则包 key',
    finding_id      BIGINT        NOT NULL COMMENT '来源 review_finding.id',
    change_type     VARCHAR(50)   NOT NULL COMMENT 'PROMOTE_TO_RULE/SUPPRESS_PATTERN',
    title           VARCHAR(500)  NOT NULL COMMENT '规则变更标题',
    rationale       VARCHAR(1000) DEFAULT NULL COMMENT '变更理由',
    status          VARCHAR(30)   NOT NULL DEFAULT 'PROPOSED' COMMENT 'PROPOSED/APPLIED/REJECTED',
    created_by      VARCHAR(100)  DEFAULT 'operations' COMMENT '创建来源',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_governance_rule_pack_change_finding (finding_id),
    INDEX idx_governance_rule_pack_change_pack (rule_pack_key),
    INDEX idx_governance_rule_pack_change_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治理规则包变更记录';
