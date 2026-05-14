-- ============================================================
-- V1__init_schema.sql
-- 初始化 review_agent 数据库所有表结构
-- ============================================================

CREATE TABLE IF NOT EXISTS project (
    id           BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name         VARCHAR(200)    NOT NULL                   COMMENT '项目名称',
    repo_url     VARCHAR(500)    NOT NULL                   COMMENT 'Git 仓库地址',
    default_branch VARCHAR(100)  NOT NULL DEFAULT 'main'    COMMENT '默认分支',
    local_path   VARCHAR(500)    DEFAULT NULL               COMMENT '本地克隆路径',
    status       VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CLONING/READY/ERROR',
    description  VARCHAR(1000)   DEFAULT NULL               COMMENT '描述',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';


CREATE TABLE IF NOT EXISTS review (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id     BIGINT       NOT NULL                    COMMENT '关联项目 ID',
    source_branch  VARCHAR(200) DEFAULT NULL                COMMENT '源分支',
    target_branch  VARCHAR(200) DEFAULT NULL                COMMENT '目标分支',
    source_commit  VARCHAR(64)  DEFAULT NULL                COMMENT '源 commit SHA',
    target_commit  VARCHAR(64)  DEFAULT NULL                COMMENT '目标 commit SHA',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING'  COMMENT 'PENDING/RUNNING/COMPLETED/FAILED',
    review_mode    VARCHAR(20)  NOT NULL DEFAULT 'SINGLE'   COMMENT 'SINGLE/MULTI/JUDGE',
    models_config  TEXT         DEFAULT NULL                COMMENT 'JSON 格式的模型配置',
    summary        TEXT         DEFAULT NULL                COMMENT '审查摘要 JSON',
    diff_content   LONGTEXT     DEFAULT NULL                COMMENT 'diff 原始内容',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码审查任务表';


CREATE TABLE IF NOT EXISTS review_finding (
    id          BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    review_id   BIGINT         NOT NULL                    COMMENT '关联审查任务 ID',
    file_path   VARCHAR(500)   NOT NULL                    COMMENT '文件路径',
    line_start  INT            DEFAULT NULL                COMMENT '起始行号',
    line_end    INT            DEFAULT NULL                COMMENT '结束行号',
    category    VARCHAR(50)    NOT NULL                    COMMENT 'CODE_STYLE/BUG/PERFORMANCE/SECURITY/EXCEPTION_HANDLING/OTHER',
    severity     VARCHAR(20)    NOT NULL                    COMMENT 'BLOCKER/MAJOR/MINOR/INFO',
    title       VARCHAR(500)   NOT NULL                    COMMENT '问题标题',
    description TEXT           DEFAULT NULL                COMMENT '问题描述',
    suggestion  TEXT           DEFAULT NULL                COMMENT '改进建议',
    model_name  VARCHAR(100)   DEFAULT NULL                COMMENT '来源模型名称',
    confidence  DECIMAL(3,2)   DEFAULT NULL                COMMENT '置信度 0.00~1.00',
    is_cross_hit TINYINT       DEFAULT 0                   COMMENT '是否多模型交叉命中',
    human_status VARCHAR(20)   DEFAULT 'PENDING'           COMMENT '人工审查状态：PENDING/CONFIRMED/DISMISSED',
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_review_id (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审查发现项表';


CREATE TABLE IF NOT EXISTS review_model_result (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    review_id      BIGINT       NOT NULL                    COMMENT '关联审查任务 ID',
    model_name     VARCHAR(100) NOT NULL                    COMMENT '模型名称',
    role           VARCHAR(20)  NOT NULL DEFAULT 'WORKER'   COMMENT 'WORKER/JUDGE',
    status         VARCHAR(20)  NOT NULL                    COMMENT 'PENDING/RUNNING/COMPLETED/FAILED',
    raw_result     LONGTEXT     DEFAULT NULL                COMMENT '模型返回原始结果 JSON',
    error_message  TEXT         DEFAULT NULL                COMMENT '错误信息',
    started_at     DATETIME     DEFAULT NULL                COMMENT '开始时间',
    completed_at   DATETIME     DEFAULT NULL                COMMENT '完成时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_review_id (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型审查执行结果表';
