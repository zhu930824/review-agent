ALTER TABLE project
    ADD COLUMN clone_error_message TEXT DEFAULT NULL COMMENT '最近一次克隆失败信息'
        AFTER description;

UPDATE project
SET clone_error_message = description,
    description = NULL
WHERE description LIKE '仓库克隆失败:%';
