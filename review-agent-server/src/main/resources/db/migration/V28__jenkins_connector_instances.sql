ALTER TABLE integration_ci_config
    ADD COLUMN display_name VARCHAR(150) NULL COMMENT 'Human-readable connector instance name',
    ADD COLUMN project_id BIGINT NULL COMMENT 'Optional project scope for this connector instance';

UPDATE integration_ci_config
SET display_name = CASE
    WHEN provider = 'JENKINS' THEN 'Default Jenkins'
    WHEN provider = 'GITLAB' THEN 'Default GitLab'
    WHEN provider = 'GITHUB' THEN 'Default GitHub'
    ELSE connector_key
END
WHERE display_name IS NULL;

CREATE INDEX idx_ci_config_provider_project
    ON integration_ci_config (provider, project_id);
