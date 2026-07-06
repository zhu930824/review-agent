ALTER TABLE integration_ci_config
    ADD COLUMN jenkins_parameter_template TEXT DEFAULT NULL COMMENT 'Optional Jenkins buildWithParameters key=value template';
