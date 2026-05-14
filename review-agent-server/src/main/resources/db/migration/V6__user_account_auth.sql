CREATE TABLE IF NOT EXISTS user_account (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username      VARCHAR(80)  NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
    display_name  VARCHAR(100) NOT NULL COMMENT '显示名称',
    email         VARCHAR(200) DEFAULT NULL COMMENT '邮箱',
    role          VARCHAR(50)  NOT NULL DEFAULT '管理员' COMMENT '角色',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_account_username (username),
    UNIQUE KEY uk_user_account_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';
