# Tasks

- [x] Task 1: 后端模型配置 API（ModelConfig）
  - [x] SubTask 1.1: 创建 Entity 类（ModelProvider、ModelProfile、ReviewStrategy、ReviewStrategyModel）映射 V2 表
  - [x] SubTask 1.2: 创建 Mapper 接口（ModelProviderMapper、ModelProfileMapper、ReviewStrategyMapper、ReviewStrategyModelMapper）
  - [x] SubTask 1.3: 创建 VO 类（ModelProviderVO、ModelProfileVO、ReviewStrategyVO），含 StrategyRoleBindingVO 子对象
  - [x] SubTask 1.4: 创建 ModelConfigService 接口与实现，实现查询供应商、档案、策略列表，策略需关联角色绑定和模型档案
  - [x] SubTask 1.5: 创建 ModelConfigController，暴露 `GET /api/model-config/providers`、`GET /api/model-config/profiles`、`GET /api/model-config/strategies`

- [x] Task 2: 后端治理能力目录 API（Governance）
  - [x] SubTask 2.1: 创建 Entity 类（GovernanceCapability、IntegrationConnector、GovernanceRulePack、WorkflowTemplate）映射 V3 表
  - [x] SubTask 2.2: 创建 Mapper 接口（GovernanceCapabilityMapper、IntegrationConnectorMapper、GovernanceRulePackMapper、WorkflowTemplateMapper）
  - [x] SubTask 2.3: 创建 VO 类（GovernanceCapabilityVO、IntegrationConnectorVO、GovernanceRulePackVO、WorkflowTemplateVO、CapabilityCoverageVO）
  - [x] SubTask 2.4: 创建 GovernanceService 接口与实现，实现查询所有能力、连接器、规则包、工作流模板
  - [x] SubTask 2.5: 创建 GovernanceController，暴露 `GET /api/governance/capabilities`、`GET /api/governance/connectors`、`GET /api/governance/rule-packs`、`GET /api/governance/workflows`

- [x] Task 3: 后端运营数据 API（Operations）
  - [x] SubTask 3.1: 创建 OperationsVO 类（含 finding 列表、指标汇总、规则学习候选）
  - [x] SubTask 3.2: 创建 OperationsService 接口与实现，从 review_finding 表查询真实数据，计算运营仪表盘指标
  - [x] SubTask 3.3: 创建 OperationsController，暴露 `GET /api/operations/dashboard`

- [x] Task 4: 前端治理中心页面改造
  - [x] SubTask 4.1: 在 `pages/governance/index.vue` 中移除对 `governanceCatalog.ts` 静态数组的导入
  - [x] SubTask 4.2: 使用 `useApi()` 分别调用 4 个治理 API，在 `onMounted` 中加载数据
  - [x] SubTask 4.3: 保留纯函数计算逻辑（`getCapabilityCoverageSummary`、`getRecommendedNextActions`、`getConnectorsByStage`、`compileGovernancePolicyPack`），数据源改为 API 响应
  - [x] SubTask 4.4: 添加数据加载状态（loading skeleton）和空数据兜底展示

- [x] Task 5: 前端模型配置页面改造
  - [x] SubTask 5.1: 在 `pages/settings/models.vue` 中移除对 `modelStrategies.ts` 静态数据的导入
  - [x] SubTask 5.2: 使用 `useApi()` 调用 3 个模型配置 API，在 `onMounted` 中加载数据
  - [x] SubTask 5.3: 保留纯函数计算逻辑（`getProviderModelCounts`、`getStrategySummaries`），数据源改为 API 响应
  - [x] SubTask 5.4: 添加数据加载状态和空数据兜底展示

- [x] Task 6: 前端运营中心页面改造
  - [x] SubTask 6.1: 在 `pages/operations/index.vue` 中移除硬编码 `sampleFindings` 和静态参数
  - [x] SubTask 6.2: 使用 `useApi()` 调用 `GET /api/operations/dashboard`，在 `onMounted` 中加载数据
  - [x] SubTask 6.3: 保留 `reviewOperations.ts` 纯函数计算逻辑作为后端返回数据的补充计算
  - [x] SubTask 6.4: 添加数据加载状态和空数据兜底展示

- [x] Task 7: 前端发起审查页策略数据源切换
  - [x] SubTask 7.1: 在 `pages/reviews/create.vue` 中将策略加载从 `builtinReviewStrategies` 切换为 API 调用
  - [x] SubTask 7.2: 调整策略编译逻辑：前端仅传递 strategyId，后端根据 strategyId 编译 modelsConfig
  - [x] SubTask 7.3: 处理策略列表加载中和加载失败状态

# Task Dependencies
- Task 1、Task 2、Task 3 可并行开发（后端三个模块独立）
- Task 4 依赖 Task 2（需要治理 API 就绪）
- Task 5 依赖 Task 1（需要模型配置 API 就绪）
- Task 6 依赖 Task 3（需要运营 API 就绪）
- Task 7 依赖 Task 1（需要策略 API 就绪）
- Task 4、Task 5、Task 6、Task 7 可并行开发（在各自依赖的后端 API 完成后）
