CREATE TABLE IF NOT EXISTS access_audit_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    actor_user_id   BIGINT       DEFAULT NULL COMMENT 'User that performed the change',
    actor_username  VARCHAR(100) DEFAULT NULL COMMENT 'Actor username snapshot',
    action_type     VARCHAR(50)  NOT NULL COMMENT 'Access change type',
    action_status   VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT 'Action result',
    target_user_id  BIGINT       DEFAULT NULL COMMENT 'Affected user account id',
    target_username VARCHAR(100) DEFAULT NULL COMMENT 'Affected username snapshot',
    project_id      BIGINT       DEFAULT NULL COMMENT 'Affected project id',
    previous_role   VARCHAR(30)  DEFAULT NULL COMMENT 'Role before change',
    new_role        VARCHAR(30)  DEFAULT NULL COMMENT 'Role after change',
    detail          VARCHAR(500) DEFAULT NULL COMMENT 'Human-readable change detail',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    INDEX idx_access_audit_actor (actor_user_id, created_at),
    INDEX idx_access_audit_target (target_user_id, created_at),
    INDEX idx_access_audit_project (project_id, created_at),
    INDEX idx_access_audit_type (action_type, created_at),
    INDEX idx_access_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Platform and project access change audit trail';
