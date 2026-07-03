-- ============================================================
-- V17__add_project_gitlab_config.sql
-- 新增 GitLab 集成配置表，支持通过 GitLab API 直接获取 diff
-- 无需在本地克隆仓库
-- ============================================================

CREATE TABLE IF NOT EXISTS project_gitlab_config (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      BIGINT       NOT NULL                    COMMENT '关联项目 ID',
    gitlab_host     VARCHAR(200) NOT NULL                    COMMENT 'GitLab 实例地址，如 https://gitlab.com',
    gitlab_token    VARCHAR(200) NOT NULL                    COMMENT 'GitLab Personal Access Token',
    project_path    VARCHAR(500) NOT NULL                    COMMENT 'GitLab 项目路径（URL 编码），如 group%2Fproject',
    enabled         TINYINT      NOT NULL DEFAULT 1          COMMENT '是否启用 API 模式',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitLab 集成配置表';
