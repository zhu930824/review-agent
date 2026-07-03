CREATE TABLE IF NOT EXISTS operations_rule_learning_decision (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    finding_id   BIGINT       NOT NULL                    COMMENT '关联 review_finding.id',
    action       VARCHAR(50)  NOT NULL                    COMMENT '候选动作：PROMOTE_TO_RULE/SUPPRESS_PATTERN',
    decision     VARCHAR(20)  NOT NULL                    COMMENT 'ACCEPTED/REJECTED',
    decided_by   VARCHAR(100) DEFAULT 'operations'         COMMENT '决策人或系统来源',
    reason       VARCHAR(1000) DEFAULT NULL                COMMENT '决策原因',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_rule_learning_decision_finding (finding_id),
    INDEX idx_rule_learning_decision_decision (decision),
    INDEX idx_rule_learning_decision_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营规则学习候选决策表';
