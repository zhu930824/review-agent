CREATE TABLE IF NOT EXISTS project_member (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    project_id BIGINT      NOT NULL COMMENT 'Project id',
    user_id    BIGINT      NOT NULL COMMENT 'User account id',
    role       VARCHAR(30) NOT NULL COMMENT 'OWNER/MAINTAINER/REVIEWER',
    created_by BIGINT      DEFAULT NULL COMMENT 'User that granted access',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_project_member (project_id, user_id),
    INDEX idx_project_member_user (user_id),
    INDEX idx_project_member_role (project_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Project membership and repository access';

INSERT IGNORE INTO project_member (project_id, user_id, role, created_by)
SELECT p.id, u.id, 'OWNER', u.id
FROM project p
JOIN user_account u ON u.role = 'ADMIN' AND u.status = 'ACTIVE';
