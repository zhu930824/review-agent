-- ============================================================
-- V3__market_governance_capabilities.sql
-- Market capability map, governance rule packs, integration roadmap.
-- ============================================================

CREATE TABLE IF NOT EXISTS governance_capability (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    capability_key  VARCHAR(100)  NOT NULL COMMENT 'Stable capability key',
    name            VARCHAR(200)  NOT NULL COMMENT 'Capability name',
    category        VARCHAR(50)   NOT NULL COMMENT 'AI_REVIEW/QUALITY/SECURITY/INTEGRATION/KNOWLEDGE/ANALYTICS',
    status          VARCHAR(30)   NOT NULL COMMENT 'ENABLED/PARTIAL/PLANNED',
    business_impact VARCHAR(20)   NOT NULL COMMENT 'HIGH/MEDIUM/LOW',
    readiness       INT           NOT NULL DEFAULT 0 COMMENT 'Readiness score 0-100',
    market_signal   VARCHAR(1000) DEFAULT NULL COMMENT 'Observed market expectation',
    platform_move   VARCHAR(1000) DEFAULT NULL COMMENT 'Recommended platform action',
    source_products VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of reference products',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_governance_capability_key (capability_key),
    INDEX idx_governance_capability_status (status),
    INDEX idx_governance_capability_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Governance capability catalog';

CREATE TABLE IF NOT EXISTS integration_connector (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    connector_key       VARCHAR(100)  NOT NULL COMMENT 'Stable connector key',
    name                VARCHAR(200)  NOT NULL COMMENT 'Connector name',
    rollout_stage       VARCHAR(30)   NOT NULL COMMENT 'live/next/later',
    status              VARCHAR(30)   NOT NULL COMMENT 'ENABLED/PARTIAL/PLANNED',
    business_value      VARCHAR(1000) DEFAULT NULL COMMENT 'Business value',
    implementation_hint VARCHAR(1000) DEFAULT NULL COMMENT 'Implementation hint',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_integration_connector_key (connector_key),
    INDEX idx_integration_connector_stage (rollout_stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration connector roadmap';

CREATE TABLE IF NOT EXISTS governance_rule_pack (
    id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    rule_pack_key     VARCHAR(100)  NOT NULL COMMENT 'Stable rule pack key',
    name              VARCHAR(200)  NOT NULL COMMENT 'Rule pack name',
    business_outcome  VARCHAR(1000) DEFAULT NULL COMMENT 'Expected business outcome',
    controls          TEXT          DEFAULT NULL COMMENT 'JSON array of controls',
    capability_keys   VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of required capability keys',
    strategy_keys     VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of suggested strategy keys',
    human_checkpoints VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of human checkpoints',
    enabled           TINYINT       NOT NULL DEFAULT 1 COMMENT 'Enabled flag',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_governance_rule_pack_key (rule_pack_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Governance rule pack';

CREATE TABLE IF NOT EXISTS workflow_template (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    template_key     VARCHAR(100)  NOT NULL COMMENT 'Stable workflow template key',
    name             VARCHAR(200)  NOT NULL COMMENT 'Template name',
    scenario         VARCHAR(1000) DEFAULT NULL COMMENT 'Applicable scenario',
    strategy_key     VARCHAR(100)  NOT NULL COMMENT 'Review strategy key',
    rule_pack_keys   VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of rule pack keys',
    connector_keys   VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of connector keys',
    success_metric   VARCHAR(1000) DEFAULT NULL COMMENT 'Expected success metric',
    enabled          TINYINT       NOT NULL DEFAULT 1 COMMENT 'Enabled flag',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_workflow_template_key (template_key),
    INDEX idx_workflow_template_strategy (strategy_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Business workflow templates';

INSERT INTO governance_capability
    (capability_key, name, category, status, business_impact, readiness, market_signal, platform_move, source_products)
VALUES
    ('pr-summary', 'PR change summary', 'AI_REVIEW', 'ENABLED', 'HIGH', 92, 'AI review products explain scope, risk, and test impact in pull requests.', 'Generate structured summaries from existing review strategies.', '["CodeRabbit","Qodo Merge","GitHub Copilot"]'),
    ('inline-review', 'Inline review suggestions', 'AI_REVIEW', 'ENABLED', 'HIGH', 88, 'Findings should map to concrete files, lines, and suggestions.', 'Keep findings tied to diff locations and remediation advice.', '["CodeRabbit","Qodo Merge","GitHub Copilot"]'),
    ('quality-gate', 'Quality gate', 'QUALITY', 'ENABLED', 'HIGH', 90, 'Quality gates block risky merges based on reliability, security, and maintainability.', 'Use severity and human review state as Pre-PR gate inputs.', '["SonarQube","Snyk"]'),
    ('security-scan', 'Security scan and severity gating', 'SECURITY', 'PARTIAL', 'HIGH', 66, 'Security products prevent new vulnerabilities from entering the codebase.', 'Map security findings to vulnerability, hotspot, and owner confirmation flows.', '["Snyk","SonarQube","CodeRabbit"]'),
    ('sarif-export', 'SARIF export', 'INTEGRATION', 'PLANNED', 'MEDIUM', 48, 'Code scanning ecosystems ingest SARIF and show alerts in PR checks.', 'Export review findings as SARIF for GitHub code scanning.', '["GitHub Code Scanning","Snyk"]'),
    ('ci-status-check', 'CI status check', 'INTEGRATION', 'PLANNED', 'HIGH', 52, 'PR protection needs pass/fail checks from automated review tools.', 'Add status callback for GitHub and GitLab merge gates.', '["GitHub","Snyk","SonarQube"]')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    readiness = VALUES(readiness),
    platform_move = VALUES(platform_move),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO integration_connector
    (connector_key, name, rollout_stage, status, business_value, implementation_hint)
VALUES
    ('local-repository', 'Local repository review', 'live', 'ENABLED', 'Run checks before a PR is opened.', 'Reuse current project, branch, and diff review flow.'),
    ('github-checks', 'GitHub Checks / Status', 'next', 'PLANNED', 'Make Review Agent visible in branch protection.', 'Add webhook receiver, status writer, and SARIF upload task.'),
    ('sarif-code-scanning', 'SARIF code scanning', 'next', 'PLANNED', 'Expose alerts in standard security views.', 'Convert findings to SARIF result entries.'),
    ('jira-linear-sync', 'Jira / Linear issue sync', 'later', 'PLANNED', 'Track accepted technical debt outside the PR.', 'Create external issues after human confirmation.')
ON DUPLICATE KEY UPDATE
    rollout_stage = VALUES(rollout_stage),
    status = VALUES(status),
    implementation_hint = VALUES(implementation_hint),
    updated_at = CURRENT_TIMESTAMP;
