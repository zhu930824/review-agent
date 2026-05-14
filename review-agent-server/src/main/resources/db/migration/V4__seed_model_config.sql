-- ============================================================
-- V4__seed_model_config.sql
-- 初始化模型供应商、模型档案、审查策略数据
-- ============================================================

-- 插入模型供应商
INSERT INTO model_provider (provider_key, name, provider_type, endpoint, api_key_env, enabled, description) VALUES
('dashscope', '阿里云 DashScope', 'DASHSCOPE', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'DASHSCOPE_API_KEY', 1, '默认模型供应商，适合通义千问系列模型。'),
('openai-compatible', 'OpenAI Compatible', 'OPENAI_COMPATIBLE', 'https://api.openai.com/v1', 'OPENAI_API_KEY', 1, '兼容 OpenAI API 协议的模型服务。'),
('custom', '自定义供应商', 'CUSTOM', 'http://localhost:11434/v1', 'CUSTOM_MODEL_API_KEY', 0, '用于企业内网、私有化或本地模型。');

-- 插入模型档案
INSERT INTO model_profile (provider_id, profile_key, model_name, display_name, capability_tags, default_temperature, timeout_seconds, enabled) VALUES
((SELECT id FROM model_provider WHERE provider_key = 'dashscope'), 'qwen-plus', 'qwen-plus', '通义千问 Plus', '["代码审查", "日常质量检查", "成本均衡"]', 0.30, 120, 1),
((SELECT id FROM model_provider WHERE provider_key = 'dashscope'), 'qwen-max', 'qwen-max', '通义千问 Max', '["Judge", "架构评审", "复杂推理"]', 0.20, 180, 1),
((SELECT id FROM model_provider WHERE provider_key = 'openai-compatible'), 'deepseek-v3', 'deepseek-v3', 'DeepSeek V3', '["交叉审查", "代码理解", "缺陷发现"]', 0.25, 180, 1),
((SELECT id FROM model_provider WHERE provider_key = 'openai-compatible'), 'kimi-k2', 'kimi-k2', 'Kimi K2', '["长上下文", "文档理解", "PR 摘要"]', 0.30, 180, 1),
((SELECT id FROM model_provider WHERE provider_key = 'custom'), 'local-coder', 'local-coder', '本地代码模型', '["私有化", "离线审查"]', 0.20, 240, 0);

-- 插入审查策略
INSERT INTO review_strategy (strategy_key, name, review_mode, description, recommended_for, block_on, require_human_review_on, advisory_on, enabled) VALUES
('fast-scan', '快速自查', 'SINGLE', '单模型快速发现基础规范、低风险缺陷和明显遗漏。', '["日常小改动", "提交前自检", "低风险配置变更"]', '["BLOCKER"]', '[]', '["MAJOR", "MINOR", "INFO"]', 1),
('cross-check', '多模型交叉审查', 'MULTI', '用不同模型互相补位，提升缺陷覆盖面和交叉命中可信度。', '["中等风险需求", "多人协作代码", "需要更高召回率的改动"]', '["BLOCKER"]', '["MAJOR"]', '["MINOR", "INFO"]', 1),
('quality-gate', 'Pre-PR 质量闸门', 'AGENT', '多智能体并行审查，覆盖安全、性能、规范、异常处理和架构风险。', '["正式 PR 前", "核心链路改动", "高风险业务需求"]', '["BLOCKER"]', '["MAJOR"]', '["MINOR", "INFO"]', 1),
('architecture-board', '架构委员会评审', 'JUDGE', 'Worker 模型先审查，Judge 模型统一裁决，适合架构或复杂业务边界调整。', '["架构调整", "领域模型升级", "高影响范围重构"]', '["BLOCKER"]', '["MAJOR"]', '["MINOR", "INFO"]', 1);

-- 插入策略角色绑定
-- fast-scan
INSERT INTO review_strategy_model (strategy_id, model_profile_id, role, skills, temperature, sort_order) VALUES
((SELECT id FROM review_strategy WHERE strategy_key = 'fast-scan'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-plus'), 'WORKER', '["JavaCodeStyleSkill"]', 0.30, 0);

-- cross-check
INSERT INTO review_strategy_model (strategy_id, model_profile_id, role, skills, temperature, sort_order) VALUES
((SELECT id FROM review_strategy WHERE strategy_key = 'cross-check'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-plus'), 'WORKER', '["JavaCodeStyleSkill", "PerformanceSkill"]', 0.30, 0),
((SELECT id FROM review_strategy WHERE strategy_key = 'cross-check'), (SELECT id FROM model_profile WHERE profile_key = 'deepseek-v3'), 'WORKER', '["SecuritySkill", "ExceptionHandlingSkill"]', 0.25, 1);

-- quality-gate (AGENT 模式)
INSERT INTO review_strategy_model (strategy_id, model_profile_id, role, skills, temperature, sort_order) VALUES
((SELECT id FROM review_strategy WHERE strategy_key = 'quality-gate'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-max'), 'SECURITY_AUDITOR', '["SecuritySkill"]', 0.20, 0),
((SELECT id FROM review_strategy WHERE strategy_key = 'quality-gate'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-plus'), 'PERFORMANCE_ANALYST', '["PerformanceSkill"]', 0.20, 1),
((SELECT id FROM review_strategy WHERE strategy_key = 'quality-gate'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-plus'), 'CODE_STYLE_CHECKER', '["JavaCodeStyleSkill"]', 0.30, 2),
((SELECT id FROM review_strategy WHERE strategy_key = 'quality-gate'), (SELECT id FROM model_profile WHERE profile_key = 'deepseek-v3'), 'EXCEPTION_HANDLER', '["ExceptionHandlingSkill"]', 0.20, 3),
((SELECT id FROM review_strategy WHERE strategy_key = 'quality-gate'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-max'), 'ARCHITECT_REVIEWER', '["JavaCodeStyleSkill", "PerformanceSkill"]', 0.20, 4);

-- architecture-board (JUDGE 模式)
INSERT INTO review_strategy_model (strategy_id, model_profile_id, role, skills, temperature, sort_order) VALUES
((SELECT id FROM review_strategy WHERE strategy_key = 'architecture-board'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-plus'), 'WORKER', '["JavaCodeStyleSkill", "PerformanceSkill"]', 0.30, 0),
((SELECT id FROM review_strategy WHERE strategy_key = 'architecture-board'), (SELECT id FROM model_profile WHERE profile_key = 'deepseek-v3'), 'WORKER', '["SecuritySkill", "ExceptionHandlingSkill"]', 0.25, 1),
((SELECT id FROM review_strategy WHERE strategy_key = 'architecture-board'), (SELECT id FROM model_profile WHERE profile_key = 'qwen-max'), 'JUDGE', '[]', 0.20, 2);
