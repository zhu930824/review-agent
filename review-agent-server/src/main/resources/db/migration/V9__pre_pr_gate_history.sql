CREATE TABLE IF NOT EXISTS pre_pr_gate_history (
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    review_id    BIGINT        NOT NULL                    COMMENT '关联审查任务 ID',
    gate_status  VARCHAR(32)   NOT NULL                    COMMENT 'Gate 状态',
    event_type   VARCHAR(32)   NOT NULL                    COMMENT '事件类型：INITIALIZED/MANUAL_DECISION',
    reason       TEXT          DEFAULT NULL                COMMENT '事件原因',
    operator     VARCHAR(100)  DEFAULT NULL                COMMENT '操作者',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_pre_pr_gate_history_review_id (review_id),
    INDEX idx_pre_pr_gate_history_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pre-PR Gate 状态历史表';
