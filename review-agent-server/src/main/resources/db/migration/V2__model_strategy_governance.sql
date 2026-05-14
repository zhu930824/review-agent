-- ============================================================
-- V2__model_strategy_governance.sql
-- 多模型配置、审查策略与 Pre-PR 质量闸门
-- ============================================================

CREATE TABLE IF NOT EXISTS model_provider (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    provider_key    VARCHAR(100)  NOT NULL COMMENT '供应商唯一标识',
    name            VARCHAR(200)  NOT NULL COMMENT '供应商名称',
    provider_type   VARCHAR(50)   NOT NULL COMMENT 'DASHSCOPE/OPENAI_COMPATIBLE/CUSTOM',
    endpoint        VARCHAR(500)  NOT NULL COMMENT 'API endpoint',
    api_key_env     VARCHAR(100)  NOT NULL COMMENT 'API Key 环境变量名',
    enabled         TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用',
    description     VARCHAR(1000) DEFAULT NULL COMMENT '说明',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_model_provider_key (provider_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型供应商配置表';

CREATE TABLE IF NOT EXISTS model_profile (
    id                    BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    provider_id           BIGINT        NOT NULL COMMENT '供应商 ID',
    profile_key           VARCHAR(100)  NOT NULL COMMENT '模型档案唯一标识',
    model_name            VARCHAR(200)  NOT NULL COMMENT '模型真实名称',
    display_name          VARCHAR(200)  NOT NULL COMMENT '展示名称',
    capability_tags       VARCHAR(1000) DEFAULT NULL COMMENT '能力标签 JSON',
    default_temperature   DECIMAL(3,2)  NOT NULL DEFAULT 0.30 COMMENT '默认温度',
    timeout_seconds       INT           NOT NULL DEFAULT 120 COMMENT '超时时间',
    enabled               TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_model_profile_key (profile_key),
    INDEX idx_model_profile_provider (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型档案表';

CREATE TABLE IF NOT EXISTS review_strategy (
    id                       BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    strategy_key             VARCHAR(100)  NOT NULL COMMENT '策略唯一标识',
    name                     VARCHAR(200)  NOT NULL COMMENT '策略名称',
    review_mode              VARCHAR(20)   NOT NULL COMMENT 'SINGLE/MULTI/JUDGE/AGENT',
    description              VARCHAR(1000) DEFAULT NULL COMMENT '说明',
    recommended_for          VARCHAR(1000) DEFAULT NULL COMMENT '适用场景 JSON',
    block_on                 VARCHAR(500)  NOT NULL DEFAULT '["BLOCKER"]' COMMENT '阻断级别 JSON',
    require_human_review_on  VARCHAR(500)  NOT NULL DEFAULT '["MAJOR"]' COMMENT '人工复核级别 JSON',
    advisory_on              VARCHAR(500)  NOT NULL DEFAULT '["MINOR","INFO"]' COMMENT '建议关注级别 JSON',
    enabled                  TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_review_strategy_key (strategy_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审查策略表';

CREATE TABLE IF NOT EXISTS review_strategy_model (
    id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    strategy_id       BIGINT        NOT NULL COMMENT '策略 ID',
    model_profile_id  BIGINT        NOT NULL COMMENT '模型档案 ID',
    role              VARCHAR(50)   NOT NULL COMMENT 'WORKER/JUDGE/Agent 角色',
    skills            VARCHAR(1000) DEFAULT NULL COMMENT 'Skill 列表 JSON',
    temperature       DECIMAL(3,2)  DEFAULT NULL COMMENT '角色温度覆盖',
    sort_order        INT           NOT NULL DEFAULT 0 COMMENT '排序',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_strategy_model_strategy (strategy_id),
    INDEX idx_strategy_model_profile (model_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审查策略模型绑定表';

CREATE TABLE IF NOT EXISTS pre_pr_gate (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    review_id        BIGINT       NOT NULL COMMENT '审查任务 ID',
    gate_status      VARCHAR(30)  NOT NULL COMMENT 'PASSED/BLOCKED/NEEDS_HUMAN_REVIEW/RUNNING',
    summary          TEXT         DEFAULT NULL COMMENT 'PR 摘要',
    blocked_reasons  TEXT         DEFAULT NULL COMMENT '阻断原因 JSON',
    decided_by       VARCHAR(100) DEFAULT NULL COMMENT '人工决策人',
    decided_at       DATETIME     DEFAULT NULL COMMENT '人工决策时间',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_pre_pr_gate_review (review_id),
    INDEX idx_pre_pr_gate_status (gate_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pre-PR 质量闸门表';
