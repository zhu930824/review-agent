# Checklist

## 后端模型配置 API
- [x] ModelProvider Entity 正确映射 `model_provider` 表所有字段
- [x] ModelProfile Entity 正确映射 `model_profile` 表所有字段
- [x] ReviewStrategy Entity 正确映射 `review_strategy` 表所有字段
- [x] ReviewStrategyModel Entity 正确映射 `review_strategy_model` 表所有字段
- [x] 所有 Mapper 接口继承 BaseMapper，CRUD 操作正常
- [x] `GET /api/model-config/providers` 返回正确的供应商 JSON 数据
- [x] `GET /api/model-config/profiles` 返回正确的模型档案 JSON 数据（含 capabilityTags 数组）
- [x] `GET /api/model-config/strategies` 返回正确的策略 JSON 数据（含关联 roleBindings 和模型档案信息）

## 后端治理能力目录 API
- [x] GovernanceCapability Entity 正确映射 `governance_capability` 表所有字段
- [x] IntegrationConnector Entity 正确映射 `integration_connector` 表所有字段
- [x] GovernanceRulePack Entity 正确映射 `governance_rule_pack` 表所有字段
- [x] WorkflowTemplate Entity 正确映射 `workflow_template` 表所有字段
- [x] 所有 Mapper 接口继承 BaseMapper，CRUD 操作正常
- [x] `GET /api/governance/capabilities` 返回正确的市场能力 JSON 数据
- [x] `GET /api/governance/connectors` 返回正确的集成连接器 JSON 数据
- [x] `GET /api/governance/rule-packs` 返回正确的规则包 JSON 数据
- [x] `GET /api/governance/workflows` 返回正确的工作流模板 JSON 数据

## 后端运营数据 API
- [x] OperationVO 结构完整，包含 findings、关键指标、规则学习候选
- [x] `GET /api/operations/dashboard` 基于 `review_finding` 表真实数据计算
- [x] BLOCKER 计数、待确认数等指标计算正确
- [x] 规则学习候选逻辑正确：已确认且高置信度 → PROMOTE_TO_RULE；已驳回且低置信度 → SUPPRESS_PATTERN
- [x] 数据为空时返回空列表和零值，不报错

## 前端治理中心页面
- [x] 治理中心页面不再依赖 `governanceCatalog.ts` 中的静态数组
- [x] 市场能力对标表格数据来自 `GET /api/governance/capabilities`
- [x] 集成路线图数据来自 `GET /api/governance/connectors`
- [x] 规则包数据来自 `GET /api/governance/rule-packs`
- [x] 工作流模板数据来自 `GET /api/governance/workflows`
- [x] 覆盖率汇总、优先落地项等计算逻辑仍正常工作
- [x] 加载状态下展示 skeleton 或 loading 指示器
- [x] API 加载失败时有错误提示

## 前端模型配置页面
- [x] 模型配置页面不再依赖 `modelStrategies.ts` 中的静态数组
- [x] 供应商卡片数据来自 `GET /api/model-config/providers`
- [x] 模型档案表格数据来自 `GET /api/model-config/profiles`
- [x] 审查策略列表数据来自 `GET /api/model-config/strategies`
- [x] 供应商模型计数、策略摘要等计算逻辑仍正常工作
- [x] 加载状态下展示 skeleton 或 loading 指示器

## 前端运营中心页面
- [x] 运营中心页面不再使用硬编码 `sampleFindings`
- [x] 运营数据来自 `GET /api/operations/dashboard`
- [x] 修复队列、SLA 压力、责任人负载展示正确
- [x] 规则学习候选展示正确
- [x] 运营就绪度、收益估算展示正确
- [x] 加载状态下展示 skeleton 或 loading 指示器
- [x] 数据为空时展示合理的空状态

## 前端发起审查页
- [x] 策略卡片列表从 `GET /api/model-config/strategies` 获取
- [x] 策略选择与提交正常工作
- [x] 策略加载失败时有降级处理
- [x] 加载中状态展示 skeleton
