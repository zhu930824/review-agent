# 前后端数据联通 Spec

## Why
当前治理中心（`/governance`）、运营中心（`/operations`）和模型配置（`/settings/models`）三个页面完全使用前端静态数据渲染，后端虽有对应的数据库表（V2/V3 迁移脚本已包含 seed data）但未暴露任何 REST API。发起审查页的策略选择同样依赖前端硬编码的策略列表。需要将这三个页面的数据源从静态 mock 切换为真实的后端 API，实现前后端数据联通，使平台具备可运营能力。

## What Changes
- 新增后端治理能力目录 API（GovernanceCapability、IntegrationConnector、GovernanceRulePack、WorkflowTemplate 的查询接口）
- 新增后端模型配置 API（ModelProvider、ModelProfile、ReviewStrategy 的查询接口）
- 新增后端运营数据 API（聚合审查 findings 数据，提供修复队列、SLA、规则学习建议等运营视图）
- 改造前端治理中心页面：从 API 获取市场能力、集成路线图、规则包和工作流模板数据
- 改造前端运营中心页面：从 API 获取真实审查发现数据，替换硬编码 sampleFindings
- 改造前端模型配置页面：从 API 获取供应商、模型档案和审查策略数据
- 改造发起审查页：策略列表从 API 获取，策略编译逻辑移入后端或从前端仅传递 strategyId
- 前端 utils 下的纯函数计算逻辑保留，仅数据源从静态数组切换为 API 响应

## Impact
- Affected specs: code-review-agent（新增后端治理/配置/运营 API）、multi-agent-architecture（策略数据从静态切至 API）
- Affected code:
  - 后端新增：`controller/` 下新增 GovernanceController、ModelConfigController、OperationsController
  - 后端新增：`service/` 下新增对应 Service 接口与实现
  - 后端新增：`domain/entity/` 下新增治理、配置相关实体类
  - 后端新增：`infrastructure/persistence/` 下新增 Mapper 接口
  - 后端新增：`domain/dto/` 下新增对应 VO 类
  - 前端修改：`pages/governance/index.vue`、`pages/operations/index.vue`、`pages/settings/models.vue`、`pages/reviews/create.vue`
  - 前端保留：`utils/governanceCatalog.ts`、`utils/reviewOperations.ts`、`utils/modelStrategies.ts` 中的纯计算函数
  - 前端可能移除：静态数据数组（转为从 API 获取）

## ADDED Requirements

### Requirement: 治理能力目录 API
系统 SHALL 提供治理能力目录的查询接口，返回市场能力对标、集成路线图、规则包和工作流模板数据。

#### Scenario: 查询市场能力列表
- **WHEN** 前端请求 `GET /api/governance/capabilities`
- **THEN** 返回所有治理能力项（含状态、业务影响、市场信号、来源产品等）

#### Scenario: 查询集成连接器列表
- **WHEN** 前端请求 `GET /api/governance/connectors`
- **THEN** 返回所有集成连接器（含上线阶段、状态、业务价值、实施提示）

#### Scenario: 查询治理规则包列表
- **WHEN** 前端请求 `GET /api/governance/rule-packs`
- **THEN** 返回所有规则包（含业务产出、控制项、关联能力和策略）

#### Scenario: 查询工作流模板列表
- **WHEN** 前端请求 `GET /api/governance/workflows`
- **THEN** 返回所有工作流模板（含场景、关联策略、规则包、成功指标）

### Requirement: 模型配置 API
系统 SHALL 提供模型配置的查询接口，返回供应商、模型档案和审查策略数据。

#### Scenario: 查询模型供应商列表
- **WHEN** 前端请求 `GET /api/model-config/providers`
- **THEN** 返回所有模型供应商（含类型、端点、启用状态）

#### Scenario: 查询模型档案列表
- **WHEN** 前端请求 `GET /api/model-config/profiles`
- **THEN** 返回所有模型档案（含能力标签、温度、超时、启用状态）

#### Scenario: 查询审查策略列表
- **WHEN** 前端请求 `GET /api/model-config/strategies`
- **THEN** 返回所有审查策略（含角色绑定、门禁策略、适用场景），角色绑定中包含关联的模型档案信息

### Requirement: 运营数据 API
系统 SHALL 提供运营数据的聚合查询接口，基于真实审查发现数据生成修复队列和运营指标。

#### Scenario: 查询运营数据
- **WHEN** 前端请求 `GET /api/operations/dashboard`
- **THEN** 返回运营仪表盘数据，包含：
  - 审查发现列表（用于生成修复队列）
  - 关键指标（总发现数、BLOCKER 数、待确认数、SLA 压力、运营就绪度）
  - 规则学习候选（已确认高置信度 / 已驳回低置信度发现项）

#### Scenario: 运营数据为空时
- **WHEN** 系统尚无任何审查记录
- **THEN** 返回空列表和零值指标，前端正常展示空状态

### Requirement: 发起审查页策略数据源切换
系统 SHALL 使发起审查页的策略选择列表从后端 API 获取，而非前端硬编码。

#### Scenario: 加载可用策略
- **WHEN** 用户进入发起审查页
- **THEN** 前端从 `GET /api/model-config/strategies` 获取所有启用策略，展示为可选策略卡片

#### Scenario: 策略编译
- **WHEN** 用户选择策略并发起审查
- **THEN** 前端将 strategyId 随请求发送，后端根据 strategyId 查询策略配置并编译为审查模型配置
