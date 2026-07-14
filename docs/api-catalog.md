# Review Agent API 清单

> 更新时间：2026-07-03

本文档汇总当前项目已确认的 API。来源分为两类：

- **后端可读源码确认**：直接从可读 Controller 文件确认。
- **前端调用确认**：从前端页面或组合函数里的 `useApi` / `fetch` 调用确认。部分对应后端源码文件当前读取为受保护内容，因此先按调用契约记录。

后端统一响应体为 `Result<T>`，前端业务请求统一走 `/api`。

## Auth

来源：后端可读源码确认，`AuthController`。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | 注册并返回登录态 | 请求体：`RegisterRequest` |
| `POST` | `/api/auth/login` | 登录并返回登录态 | 请求体：`AuthRequest` |
| `POST` | `/api/auth/logout` | 退出登录 | 前端 Header/Layout 统一通过 `useAuth.logout()` 调用；当前后端不维护 token 黑名单 |
| `GET` | `/api/auth/access-profile` | 查询当前访问身份 | 返回数据库中的角色、规范化平台角色和实时权限集合 |
| `GET` | `/api/access/users` | 查询平台用户角色 | 仅 `ADMIN`，供治理中心访问控制区域使用 |
| `PATCH` | `/api/access/users/{userId}/role` | 更新平台角色 | 仅 `ADMIN`；角色限 `ADMIN`、`GOVERNANCE_MANAGER`、`OPERATOR`、`REVIEWER`，禁止移除最后一个管理员 |
| `GET` | `/api/access/audit-logs` | 查询最近权限变更审计 | 仅 `ADMIN`；支持 `limit`、`actionType`、`projectId` 筛选，最多返回 100 条 |

前端认证存储：

| Key | 用途 |
| --- | --- |
| `review-agent-token` | JWT token |
| `review-agent-user` | 当前用户信息 |

受限路由会读取 `/api/auth/access-profile`，治理中心、运营中心、AI Gateway 和模型配置按实时权限显示。除登录、注册、GitHub Webhook、GitLab Webhook 和 OpenAPI 文档外，`/api/**` 默认要求有效 JWT；随后按数据库中的实时角色执行 RBAC。首个注册账号规范化为 `ADMIN`，后续自助注册账号规范化为 `REVIEWER`。平台角色变更和项目成员增删改会在同一事务写入 `access_audit_log`，治理中心可按动作类型和项目查看最近证据链。

## Projects

来源：后端可读源码确认，`ProjectController`。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `POST` | `/api/projects` | 创建项目 | 请求体：`CreateProjectRequest` |
| `GET` | `/api/projects` | 分页查询项目 | 参数：`pageNum`、`pageSize` 等 |
| `GET` | `/api/projects/{id}` | 查询项目详情 |  |
| `PUT` | `/api/projects/{id}` | 更新项目 | 请求体：`UpdateProjectRequest` |
| `DELETE` | `/api/projects/{id}` | 删除项目 |  |
| `POST` | `/api/projects/{id}/retry-clone` | 重试仓库克隆 |  |
| `GET` | `/api/projects/{id}/branches` | 查询项目分支 | 项目详情页展示；Review 创建页使用 |
| `GET` | `/api/projects/{id}/members` | 查询项目成员 | 项目成员可读；返回 `OWNER`、`MAINTAINER`、`REVIEWER` 及权限集合 |
| `PUT` | `/api/projects/{id}/members` | 添加成员或更新项目角色 | 仅项目 Owner 或平台 Admin；请求体传 `username`、`role` |
| `DELETE` | `/api/projects/{id}/members/{userId}` | 移除项目成员 | 仅项目 Owner 或平台 Admin；禁止移除最后一个 Owner |

当前前端接入状态：

- 项目详情页读取 `/api/projects/{id}/branches` 展示仓库分支列表，并支持手动刷新；非默认分支可直接跳转创建 Review，并通过 query 预填 `projectId`、`sourceBranch`、`targetBranch`。创建 Review 页也使用该接口作为源分支/目标分支选项，并校验 URL 预填分支仍存在。
- 项目列表仅返回当前用户参与的项目，平台 Admin 可应急查看全部项目。创建者自动成为 Owner；项目详情页可按用户名添加成员、调整角色和移除成员，并按当前项目角色隐藏编辑、删除和 GitLab 策略操作。
- 项目创建者 Owner 分配、成员添加、项目角色变更和成员移除均写入访问审计；重复提交相同角色不会生成无效审计记录。

## Reviews

来源：前端调用确认。对应部分后端源码当前不可稳定文本读取。

| Method | Path | 用途 | 前端使用位置 |
| --- | --- | --- | --- |
| `POST` | `/api/reviews` | 创建普通 Review | 旧入口，后端源码当前受保护 |
| `POST` | `/api/reviews/pre-pr` | 创建 Pre-PR 审查 | `views/reviews/create.vue`；请求体携带项目、源/目标分支、策略 Key、reviewMode 和 modelsConfig；创建成功后前端初始化 Gate |
| `GET` | `/api/reviews` | 分页查询 Review | Dashboard、项目详情 |
| `GET` | `/api/reviews/{id}` | 查询 Review 详情 | Review 详情页 |
| `PATCH` | `/api/reviews/{reviewId}/finding/{findingId}` | 更新 Finding 人工状态 | Review 详情页 |
| `GET` | `/api/reviews/{id}/progress` | SSE 审查进度 | Review 详情页；原生 EventSource 通过受限 `access_token` query 参数鉴权，仍校验项目成员权限 |
| `GET` | `/api/reviews/{id}/risk` | 获取风险预测 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/generate-tests` | 生成测试覆盖计划 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/refactor-plan` | 生成重构计划 | Review 详情页智能分析 |

所有 Review 资源都会先由 Review 或 Finding 反查 `project_id`：读操作要求项目 `VIEW`，创建、Finding 状态、Gate 决策和分析操作要求 `REVIEW_EXECUTE`。平台 Admin 可全局访问；签名验证后的 GitLab Webhook 作为系统触发保留自动 Review 能力。

### Pre-PR Gate

来源：后端可读源码确认，`PrePrGateController`；前端 Review 详情页已接入。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `GET` | `/api/reviews/{id}/gate` | 查询或初始化后端持久化 Gate 状态 | 优先读取 `pre_pr_gate`，不存在时按 Review/Finding 计算并写入 |
| `POST` | `/api/reviews/{id}/gate/initialize` | 初始化 Pre-PR Gate | Pre-PR 创建成功后调用；写入 `pre_pr_gate_history` 初始化事件；不发布 CI |
| `POST` | `/api/reviews/{id}/gate/refresh` | 重新计算并持久化 Gate 状态 | Finding 人工状态变化后使用；成功后自动发布 CI 状态 |
| `POST` | `/api/reviews/{id}/gate/publish-ci` | 将持久化 Gate 状态发布到 CI/PR 状态系统 | `PASSED` -> success，`BLOCKED` / `NEEDS_HUMAN_REVIEW` -> failure，`RUNNING` -> pending；Review 详情页提供手动重发入口 |
| `PATCH` | `/api/reviews/{id}/gate/decision` | 记录人工 Gate 决策 | 请求体：`PrePrGateDecisionRequest`；写入 `pre_pr_gate_history` 决策事件；成功后自动发布 CI 状态 |

`PrePrGateDecisionRequest`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `gateStatus` | string | `PASSED`、`BLOCKED`、`NEEDS_HUMAN_REVIEW`，兼容 `APPROVED` -> `PASSED` |
| `reason` | string | 人工决策原因 |
| `decidedBy` | string | 决策人 |

`PrePrGateVO`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `reviewId` | number | Review ID |
| `gateStatus` | string | `PASSED`、`BLOCKED`、`NEEDS_HUMAN_REVIEW`、`RUNNING` |
| `blockedReasons` | string[] | 后端持久化阻断或人工决策原因 |
| `decidedBy` | string \| null | 人工决策人 |
| `decidedAt` | string \| null | 人工决策时间 |

当前阶段状态：

- `POST /api/reviews/pre-pr` 已由前端创建页使用，后端源码当前受保护，按调用契约记录。创建页会把所选策略编排成 `modelsConfig`，高级 JSON 覆盖会先做格式校验，提交时随 `strategyKey`、`strategyId`、`reviewMode` 一起发送。
- Pre-PR 创建成功后，前端调用 `POST /api/reviews/{id}/gate/initialize`，确保进入详情页前已有持久化 Gate 和初始化历史。
- CI 状态发布已读取持久化 Gate；后端会发布到所有启用的 `github-checks`、`gitlab-merge-request`、`jenkins-pipeline` 配置。GitLab 走 Commit Status API，Jenkins 会先尝试读取 crumb，再触发 `buildWithParameters` Job；默认携带 Review Agent Gate 参数，并支持通过 Jenkins 参数模板追加或覆盖参数。

## Model Config

来源：后端可读源码确认，`ModelConfigController`。

### Providers

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/model-config/providers` | 查询模型供应商列表 |
| `GET` | `/api/model-config/providers/{id}` | 查询模型供应商详情 |
| `POST` | `/api/model-config/providers` | 创建模型供应商 |
| `PUT` | `/api/model-config/providers/{id}` | 更新模型供应商 |
| `DELETE` | `/api/model-config/providers/{id}` | 删除模型供应商 |

### Profiles

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/model-config/profiles` | 查询模型档案列表 |
| `GET` | `/api/model-config/profiles/{id}` | 查询模型档案详情 |
| `POST` | `/api/model-config/profiles` | 创建模型档案 |
| `PUT` | `/api/model-config/profiles/{id}` | 更新模型档案 |
| `DELETE` | `/api/model-config/profiles/{id}` | 删除模型档案 |

### Strategies

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/model-config/strategies` | 查询审查策略列表 |
| `GET` | `/api/model-config/strategies/{id}` | 查询审查策略详情 |
| `POST` | `/api/model-config/strategies` | 创建审查策略 |
| `PUT` | `/api/model-config/strategies/{id}` | 更新审查策略 |
| `DELETE` | `/api/model-config/strategies/{id}` | 删除审查策略 |

### Invocations

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `POST` | `/api/model-config/invocations/smoke-test` | 发起一次模型调用烟测 | 走当前 `ModelInvocationPort`，HTTP adapter 开启时会调用真实供应商并写入遥测；未配置时返回 `FAILED` 和可读错误信息 |

当前前端接入状态：

- 模型配置页 `/settings/models` 已接入 Provider、Profile、Strategy 列表读取，并提供新增、编辑、删除入口；Strategy 表单支持维护推荐场景、Gate 级别和角色到模型档案的绑定。
- 模型配置页已接入 `/api/model-config/invocations/smoke-test`，可选择启用的模型档案发起最小调用，并展示 `SUCCESS/FAILED`、错误信息、返回正文、token 和成本字段。

## Governance

来源：后端可读源码确认，`GovernanceController`。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/governance/capabilities` | 查询治理能力目录 |
| `GET` | `/api/governance/connectors` | 查询集成连接器路线图 |
| `GET` | `/api/governance/rule-packs` | 查询治理规则包 |
| `GET` | `/api/governance/rule-pack-changes` | 查询最近规则包变更记录 |
| `GET` | `/api/governance/rule-pack-versions` | 查询最近规则包版本快照 |
| `POST` | `/api/governance/rule-pack-changes/{id}/approve` | 批准规则包变更 | 状态更新为 `APPROVED` |
| `POST` | `/api/governance/rule-pack-changes/{id}/dry-run` | 预览规则包变更 | 返回已有控制项数量、拟新增控制项、快照 JSON 和影响摘要；不写库、不改状态 |
| `POST` | `/api/governance/rule-pack-changes/{id}/apply` | 应用规则包变更 | 写回 `governance_rule_pack.controls`，生成 `governance_rule_pack_version` 快照，并将状态更新为 `APPLIED` |
| `POST` | `/api/governance/rule-pack-changes/{id}/reject` | 拒绝规则包变更 | 状态更新为 `REJECTED` |
| `POST` | `/api/governance/rule-pack-changes/{id}/rollback` | 回滚规则包变更 | 从 `governance_rule_pack.controls` 移除对应控制项，将版本状态标为 `ROLLED_BACK`，并将变更状态更新为 `ROLLED_BACK` |
| `GET` | `/api/governance/workflows` | 查询工作流模板 |

当前前端接入状态：

- 治理中心已展示最近规则包变更和规则包版本，分别读取 `/api/governance/rule-pack-changes?limit=20`、`/api/governance/rule-pack-versions?limit=20`。Operations 采纳规则学习候选后，会写入 `governance_rule_pack_change`，默认归入 `team-rule-memory` 规则包，状态为 `PROPOSED`；治理中心支持 `PROPOSED -> APPROVED -> APPLIED`，以及 `REJECTED`、`ROLLED_BACK` 状态流转；应用变更时会把拟新增控制项写入 `governance_rule_pack.controls` 并生成 `governance_rule_pack_version` 快照；规则包版本卡片可查看控制项快照 JSON；回滚会移除对应控制项并标记版本回滚；应用前可通过 dry-run 预览拟新增控制项和影响摘要。

## Operations

来源：后端可读源码确认，`OperationsController`；前端运营中心读取 Operations 聚合接口形成策略压力视图。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/operations/dashboard` | 查询运营中心数据 |
| `GET` | `/api/operations/tasks` | 查询统一运营任务 | 参数：`limit`，默认 50；合并 Finding 修复项与 CI 健康异常，并叠加 `operations_task` 中的状态、关闭原因和更新时间 |
| `GET` | `/api/operations/tasks/sla-alerts` | 查询 SLA 告警任务 | 参数：`limit`，默认 20；返回 `OVERDUE` 和 `DUE_SOON` 的统一任务，包含到期时间、剩余小时数和 SLA 状态 |
| `POST` | `/api/operations/tasks/sync` | 同步统一运营任务 | 参数：`limit`，默认 50；把当前推导出的 Finding/CI 健康任务 upsert 到 `operations_task` |
| `PATCH` | `/api/operations/tasks/batch` | 批量更新统一运营任务 | 请求体传 `taskKeys`，可同时传 `status`、`ownerRole`、`slaHours`；用于批量分派任务和统一 SLA |
| `PATCH` | `/api/operations/tasks/{taskKey}` | 更新统一运营任务 | 请求体可传 `status`、`ownerRole`、`slaHours`；支持接手处理中、接受风险、Owner/SLA 调整等运营流转 |
| `POST` | `/api/operations/tasks/{taskKey}/gitlab-issue` | 同步统一运营任务到 GitLab Issue | 仅 Finding 来源任务会尝试创建 GitLab Issue；无项目 GitLab 配置时记录 `SKIPPED`，失败时记录 `FAILED` |
| `POST` | `/api/operations/tasks/{taskKey}/gitlab-issue/refresh` | 刷新 GitLab Issue 状态 | 读取已关联 GitLab Issue 的 `state`、标题、labels、作者、负责人、更新时间和关闭时间，记录到 `OperationsExternalIssueVO`；当 GitLab Issue 为 `closed` 时关闭统一运营任务。后端也会按 `review-agent.operations.gitlab-issue-refresh-delay-ms` 自动批量刷新最近的 GitLab Issue |
| `POST` | `/api/operations/tasks/{taskKey}/external-issue` | 手动绑定外部 Issue | 请求体传 `provider`、Issue id/url/state/title/labels/assignee 等字段；支持 `GITLAB`、`JIRA`、`ZENTAO`、`OTHER`，用于把企业既有 Issue 系统纳入运营任务闭环 |
| `POST` | `/api/operations/tasks/{taskKey}/close` | 关闭统一运营任务 | 请求体可传 `closeReason`；将任务状态标记为 `RESOLVED` 并记录关闭原因 |
| `GET` | `/api/operations/strategy-pressure` | 查询策略成本/质量压力排行 | 基于模型遥测 summary 生成压力分、压力等级和运营建议 |
| `GET` | `/api/operations/ci-health-actions` | 查询 CI 健康运营行动项 | 从 CI 集成健康度派生异常 connector 的 owner、SLA、最新信号、最近 writeback id、请求 URL、Jenkins queue/build URL、处理建议和通知计划 |
| `POST` | `/api/operations/ci-health-actions/{actionKey}/notify` | 发送 CI 健康通知 | 按 action key 读取当前 CI Health Action；有 `notificationWebhookUrl` 时发送通用 Webhook 并记录 `CI_HEALTH_NOTIFICATION` 动作日志，无配置时记录 `SKIPPED` |
| `GET` | `/api/operations/remediation-queue` | 查询运营中心修复队列 | 参数：`limit`，默认 50；返回未驳回 Finding，包含 Review、项目、严重度、人工状态、置信度和跨模型命中信息 |
| `POST` | `/api/operations/remediation-queue/{findingId}/confirm` | 确认修复队列风险项有效 | 将 Finding 人工状态更新为 `CONFIRMED` |
| `POST` | `/api/operations/remediation-queue/{findingId}/dismiss` | 将修复队列风险项标记为误报 | 将 Finding 人工状态更新为 `DISMISSED` |
| `GET` | `/api/operations/owner-load` | 查询运营责任人负载 | 返回按 Finding 分类映射的责任人、数量和占比 |
| `GET` | `/api/operations/rule-learning-candidates` | 查询规则学习候选 | 参数：`limit`，默认 20；返回 `PROMOTE_TO_RULE` / `SUPPRESS_PATTERN` 候选 |
| `POST` | `/api/operations/rule-learning-candidates/{findingId}/accept` | 采纳规则学习候选 | 写入 `operations_rule_learning_decision`，后续候选列表会过滤已决策项 |
| `POST` | `/api/operations/rule-learning-candidates/{findingId}/reject` | 拒绝规则学习候选 | 写入 `operations_rule_learning_decision`，后续候选列表会过滤已决策项 |
| `GET` | `/api/operations/business-impact` | 查询业务收益估算 | 基于近期 Finding 的 review 数、人工确认/驳回覆盖率、BLOCKER/MAJOR 数量估算节省审查时间和规避返工时间 |
| `GET` | `/api/operations/telemetry-readiness` | 查询模型遥测接入就绪度 | 基于模型遥测 summary 输出策略级 `READY` / `NEEDS_ATTRIBUTION` / `JUDGE_UNSTABLE` / `NOT_CONNECTED` 状态和建议 |

当前前端接入状态：

- 运营中心全局 KPI 优先读取 `/api/operations/dashboard`，统一任务表读取 `/api/operations/tasks`，SLA Alerts 面板读取 `/api/operations/tasks/sla-alerts`，并支持通过 `/api/operations/tasks/sync` 同步任务、`PATCH /api/operations/tasks/{taskKey}` 更新状态/Owner/SLA、`PATCH /api/operations/tasks/batch` 批量分派、`POST /api/operations/tasks/{taskKey}/gitlab-issue` 创建/记录 GitLab Issue、`POST /api/operations/tasks/{taskKey}/gitlab-issue/refresh` 刷新 GitLab Issue 状态并回流关闭、`POST /api/operations/tasks/{taskKey}/external-issue` 手动绑定 Jira/禅道/其他 Issue、`/api/operations/tasks/{taskKey}/close` 关闭任务；修复队列读取 `/api/operations/remediation-queue`，责任人负载读取 `/api/operations/owner-load`，规则学习视图读取 `/api/operations/rule-learning-candidates`，业务收益估算读取 `/api/operations/business-impact`；责任人负载、规则学习候选和业务收益均保留本地推导兜底。
- 运营中心修复队列已支持“确认有效”和“标记误报”，分别调用 `/api/operations/remediation-queue/{findingId}/confirm` 和 `/api/operations/remediation-queue/{findingId}/dismiss`，操作完成后刷新队列、Owner 负载、规则学习候选和业务收益估算。
- 运营中心规则学习候选已支持“采纳”和“拒绝”，分别调用 `/api/operations/rule-learning-candidates/{findingId}/accept` 和 `/api/operations/rule-learning-candidates/{findingId}/reject`；决策持久化到 `operations_rule_learning_decision`，候选列表过滤已决策项；采纳时同步生成 `governance_rule_pack_change` 变更记录。
- 运营中心“策略成本/质量压力”面板调用 `/api/operations/strategy-pressure` 和 `/api/operations/telemetry-readiness`，按失败率、误报代理、确认率、策略命中率、Judge 失败率、平均成本和延迟展示后端生成的策略压力分、压力等级、运营建议和遥测接入/归因就绪状态。
- 运营中心“CI Health Actions”面板调用 `/api/operations/ci-health-actions`，把 GitHub/GitLab/Jenkins 等 connector 的异常健康状态纳入 Owner + SLA 运营视图，并展示通知计划载荷；通知计划包含 `notificationPriority`、`notificationDedupKey`、`notificationTitle`、`notificationBody` 和 `notificationTargetUrl`。治理中心 CI 配置可保存 `notificationWebhookUrl`，页面可调用 `/api/operations/ci-health-actions/{actionKey}/notify` 发送通用 Webhook，并把 `POSTED`、`SKIPPED`、`FAILED` 写入 `integration_action_log`。页面也可直接调用 `/api/integration/ci-config/writebacks/{id}/retry` 重试失败回写、调用 `/api/integration/ci-config/writebacks/jenkins/refresh` 刷新 Jenkins 队列/构建结果。
- 治理中心“遥测行动项”面板调用 `/api/operations/telemetry-readiness`，将 `NO_TELEMETRY`、`WEAK_ATTRIBUTION`、`JUDGE_FAILURE` 等 `gapCode` 转成平台集成待办，便于从治理视角继续补齐模型调用遥测、跨模型归因和 Judge 稳定性。

## Integration

来源：后端可读源码确认，`CiStatusConfigController`；前端治理中心已接入。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `GET` | `/api/integration/ci-config` | 查询 CI 回写配置 | 支持 `connectorKey`；默认 `github-checks` |
| `GET` | `/api/integration/ci-config/list` | 查询连接器实例列表 | 可按 `provider` 筛选；Jenkins 多实例使用 `jenkins-pipeline:<instance-key>`，返回实例显示名和可选项目绑定 |
| `PUT` | `/api/integration/ci-config` | 保存 CI 回写配置 | 支持 `github-checks`、`gitlab-merge-request`、`jenkins-pipeline` 及 Jenkins 实例 key；Jenkins 支持 `displayName`、`projectId`、`jenkinsParameterTemplate`，CI Health 通知支持 `notificationWebhookUrl`，token 和 webhook secret 不在响应中明文返回 |
| `DELETE` | `/api/integration/ci-config` | 删除连接器实例 | 参数：`connectorKey`；用于治理中心移除不再使用的 Jenkins 实例 |
| `POST` | `/api/integration/ci-config/test` | 测试已保存的 CI 连接 | 参数：`connectorKey`；GitHub 读取仓库元数据，GitLab 读取项目元数据，Jenkins 读取 Job 元数据，不触发构建或写状态；返回 `SUCCESS`、`FAILED`、`SKIPPED`、请求目标、耗时和测试时间，并记录 `CONNECTION_TEST` 动作 |
| `GET` | `/api/integration/ci-config/credential-health` | 查询集成凭证静态加密健康度 | 返回 `SECURE`、`DEVELOPMENT_KEY`、`MIGRATION_REQUIRED`、`ROTATION_REQUIRED` 或 `KEY_MISMATCH`，以及当前密钥、历史密钥、明文和不可解密字段数量 |
| `POST` | `/api/integration/ci-config/credential-rotation` | 轮换集成凭证 | 请求体必须传 `confirmation: "ROTATE CREDENTIALS"`；先预检全部凭据，再在单一事务中用当前主密钥重加密，并记录 `CREDENTIAL_ROTATION` 动作审计 |
| `GET` | `/api/integration/ci-config/writebacks` | 查询最近 CI 回写记录 | 参数：`limit`，最大 50 |
| `GET` | `/api/integration/ci-config/writebacks/health` | 查询 CI 集成健康度 | 参数：`limit`，默认 50；按 GitHub/GitLab/Jenkins connector 聚合最近回写记录，返回 `HEALTHY`、`DEGRADED`、`UNHEALTHY`、`NO_DATA` |
| `POST` | `/api/integration/ci-config/writebacks/{id}/retry` | 手动重试失败回写 | 仅允许 `FAILED` 记录；重发当前持久化 Gate 状态；到期 `nextRetryAt` 记录也会由后端调度自动重试 |
| `POST` | `/api/integration/ci-config/writebacks/jenkins/refresh` | 刷新 Jenkins 队列/构建结果 | 参数：`limit`，默认 20；读取 Jenkins queue/build API，把 build URL、build number 和 result 回写到 CI 日志；后端也会按 `review-agent.ci-writeback.jenkins-refresh-delay-ms` 自动刷新 |
| `POST` | `/api/integration/webhooks/github` | 接收 GitHub webhook 投递 | 校验 `X-Hub-Signature-256`；使用 `X-GitHub-Delivery` 做幂等；写入投递日志 |
| `POST` | `/api/integration/webhooks/gitlab` | 接收 GitLab webhook 投递 | 校验 `X-Gitlab-Token` / `X-GitLab-Token`；优先使用 `X-Gitlab-Delivery` 做投递幂等，缺失时以事件类型和 payload digest 生成稳定 delivery id；已打开的 MR `open` / `reopen` / `update` 事件会按 `path_with_namespace` 匹配已接入项目并自动创建 Pre-PR Review；以项目、MR iid 和 last commit SHA 组成 revision key，在创建 Review 前唯一占位，避免不同 delivery 并发触发同一提交的重复 AI 审查，失败占位允许后续事件重试 |
| `POST` | `/api/integration/webhooks/gitlab/review-triggers/retry` | 手动重试失败的 GitLab MR Review 触发 | 请求体传 `triggerKey`；允许领取 `FAILED` 或 `EXHAUSTED` 记录，使用持久化的 projectId、sourceBranch、targetBranch 重新创建 Pre-PR Review，并将结果同步回原投递日志 |
| `GET` | `/api/integration/webhooks/gitlab/review-triggers/health` | 查询 GitLab MR Review 触发队列健康度 | 返回 `HEALTHY`、`DEGRADED`、`UNHEALTHY`，以及处理中、待重试、已耗尽、已完成数量和最早积压时间；存在 `EXHAUSTED` 时为不健康 |
| `POST` | `/api/integration/webhooks/jenkins/reviews` | Jenkins Pipeline 触发 Pre-PR Review | 公共集成入口，必须通过 `X-Review-Agent-Token` 提交对应实例的 Webhook Secret；`X-Review-Agent-Connector` 指定实例，缺省为 `jenkins-pipeline`；请求包含项目、源/目标分支、Job、Build、Build URL 和 commit SHA；按实例及 Job/Build/Commit 幂等并返回 Review ID |
| `GET` | `/api/integration/webhooks/jenkins/reviews/{reviewId}/gate` | Jenkins Pipeline 轮询 Review Gate | 使用相同 token 和实例请求头；仅允许查询由该 Jenkins 实例触发的 Review，返回 `RUNNING`、`PASSED`、`BLOCKED` 或 `NEEDS_HUMAN_REVIEW` |
| `POST` | `/api/integration/webhooks/jenkins/review-triggers/retry` | 手动重试 Jenkins Review 触发 | 需要 `INTEGRATION_MANAGE`；请求体传 `connectorKey` 和 `triggerKey`，允许领取该实例的 `FAILED` 或 `EXHAUSTED` 记录 |
| `GET` | `/api/integration/webhooks/jenkins/review-triggers/health` | 查询 Jenkins Review 触发队列健康度 | 需要 `INTEGRATION_VIEW`；参数 `connectorKey` 缺省为默认实例；健康统计、自动领取重试和积压按实例隔离 |
| `GET` | `/api/integration/webhooks/deliveries` | 查询最近 webhook 投递日志 | 参数：`limit`，最大 50；返回 provider、eventType、deliveryId、deliveryStatus、payloadDigest、错误信息、MR 审查 triggerKey、触发状态/Review ID 和接收时间 |
| `GET` | `/api/projects/{projectId}/gitlab-review-policy` | 查询项目级 GitLab 自动审核策略 | 未配置 GitLab API 的项目返回 `configured=false`；已配置项目返回自动审核开关、Draft/WIP 策略、自动 MR 摘要开关和目标分支匹配规则 |
| `PUT` | `/api/projects/{projectId}/gitlab-review-policy` | 更新项目级 GitLab 自动审核策略 | 支持 `autoReviewEnabled`、`reviewDrafts`、`publishSummaryEnabled`、`targetBranchPattern`；分支规则可用逗号或换行分隔并支持 `*` 通配符 |
| `POST` | `/api/integration/gitlab/merge-requests/summary-note` | 发布 Review 摘要到 GitLab MR Note | 请求包含 `reviewId`、可选 `mergeRequestIid` 和 Markdown `body`；Webhook 自动创建的 Review 会从触发记录解析 MR iid，手工 Review 可显式传入；结果写入 `integration_action_log`，actionType 为 `MR_SUMMARY_NOTE` |
| `POST` | `/api/integration/sarif/upload` | 上传 SARIF 到 GitHub Code Scanning | 读取 GitHub 集成配置；请求体包含 `commitSha`、`ref`、`sarif` |
| `POST` | `/api/integration/pr-summary/comment` | 回写 PR Summary 评论 | 使用 GitHub Issues comments API；请求体包含 `pullNumber`、`body` |
| `GET` | `/api/integration/actions` | 查询最近集成动作日志 | 参数：`limit`，最大 50；覆盖 SARIF 上传、PR Summary 评论等外部动作 |

当前前端接入状态：

- Review 详情页保留本地 SARIF 下载，同时新增“一键上传扫描”，会读取当前 Review 的 SARIF、`sourceCommit/targetCommit` 和分支 ref 后调用 `/api/integration/sarif/upload`。
- Review 详情页新增 “PR Summary” 操作，用户输入 Pull Request 编号后，将当前 Review 摘要、Gate 状态、严重度统计和前 5 条 Finding 组合成 Markdown 并调用 `/api/integration/pr-summary/comment`。
- 治理中心 “CI 回写就绪度” 面板新增最近集成动作与 webhook 投递日志列表，分别调用 `/api/integration/actions` 和 `/api/integration/webhooks/deliveries?limit=10` 展示外部动作状态与 GitHub/GitLab/Jenkins 投递接收、拒绝、重复状态；GitLab MR 与 Jenkins Pipeline 分别展示独立触发队列健康汇总和最早积压时间。投递记录显示 `PROCESSED`、`DEDUPLICATED`、`SKIPPED`、`FAILED`、`EXHAUSTED` 触发结果、revision key、重试次数和下次重试时间，并可跳转到新建或复用的 Review，失败及死信记录按 provider 路由到对应重试接口。
- 治理中心 CI 配置表单已支持 GitHub Checks / Status、GitLab Merge Request / Pipeline 和 Jenkins Pipeline Gate。Jenkins 可创建多个具名实例，每个实例拥有独立 URL、凭证、Webhook Secret、Job、参数模板和可选项目边界；入站触发、幂等、Gate 查询、队列健康、失败重试、出站 Gate 回写和构建结果刷新都按实例隔离。配置区优先生成 `reviewAgentGate(...)` Shared Library 用法，也保留底层 Jenkinsfile `httpRequest` 示例；仓库内 `jenkins-shared-library/` 可直接注册为 Jenkins Global Pipeline Library。Jenkins 参数模板使用换行或 `&` 分隔的 `key=value`，支持 `${reviewId}`、`${state}`、`${jenkinsState}`、`${commitSha}`、`${context}`、`${description}`、`${connectorKey}`、`${repoOwner}`、`${repoName}`、`${defaultBranch}` 占位符。后端回写日志记录 provider、实际 connectorKey、请求 URL、成功/失败/跳过原因和重试时间；Jenkins 触发成功时优先记录队列 `Location`，并支持手动与自动刷新队列/构建结果。治理中心按实例展示 Review 触发队列健康度，并把回写链路的 `UNHEALTHY`、`DEGRADED`、`NO_DATA` 转成 CI Health Actions。
- 项目详情页在 GitLab API 模式下展示“GitLab 自动审核策略”，可关闭项目自动触发、默认排除 Draft/WIP、通过 `main, release/*` 一类规则限制目标分支，并可选择在 Review 完成后自动发布 MR Summary Note。自动摘要默认关闭；发布失败不会回滚已完成的 Review，而是写入触发消息和 `MR_SUMMARY_NOTE` 动作审计。全局 GitLab connector 的 `checksEnabled=false` 同样会停止自动创建 Review；Webhook 仍进入投递日志并记录具体跳过原因。
- 治理中心 CI 配置提供“保存并测试”：先保存当前 GitHub/GitLab/Jenkins 表单，再发起只读连接验证并显示真实目标 URL、状态和耗时。Jenkins 测试仅访问 Job `/api/json`，不会触发 Pipeline；测试结果进入最近集成动作，便于审计配置上线前是否验证成功。
- `integration_ci_config.api_token`、`integration_ci_config.webhook_secret`、`project_gitlab_config.gitlab_token` 使用 AES-256-GCM `enc:v1:` 信封静态加密。应用启动时会升级旧明文，MyBatis TypeHandler 对后续读写透明加解密；生产必须设置稳定的 `REVIEW_AGENT_CREDENTIAL_KEY`。轮换时先把旧密钥以逗号分隔放入 `REVIEW_AGENT_CREDENTIAL_PREVIOUS_KEYS`，重启并确认不存在 `KEY_MISMATCH`，再通过治理中心执行事务化轮换；全部凭据归属当前密钥后移除历史密钥配置。
- Review 详情页“回流摘要”支持在 GitHub PR 与 GitLab MR 之间切换，复用当前 Review 的摘要、Gate 和主要 Finding 生成 Markdown。GitLab Webhook Review 无需再次填写 MR iid；发布结果会显示真实 `POSTED` / `SKIPPED` / `FAILED`，并进入治理中心最近集成动作列表。
- 运营中心调用 `/api/operations/ci-health-actions`，把 CI 健康异常纳入 Owner + SLA 的运营视图，并透出最近 writeback id、请求 URL、Jenkins queue/build URL；页面可直接重试失败回写或刷新 Jenkins 构建结果，便于企业集成负责人持续处理 GitLab/Jenkins/GitHub 回写异常。

## Knowledge

来源：前端调用确认。

| Method | Path | 用途 | 前端使用位置 |
| --- | --- | --- | --- |
| `GET` | `/api/knowledge/query?keyword={keyword}` | 查询知识节点 | `views/knowledge.vue` |

- 知识图谱页面已将 `/api/knowledge/query` 返回结果转成前端工作台视图：展示全部、记忆、规则、发现项数量，并支持按 `MEMORY`、`RULE`、`FINDING` 类型过滤。

## AI Gateway

来源：前端调用确认。

| Method | Path | 用途 | 前端使用位置 |
| --- | --- | --- | --- |
| `GET` | `/api/gateway/prompts` | 查询 Prompt 模板 | `views/gateway.vue` |
| `GET` | `/api/gateway/stats` | 查询模型调用统计 | `views/gateway.vue` |

## Model Telemetry

来源：后端可读源码确认，`ModelTelemetryController`；前端 AI Gateway 已优先接入 summary。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `POST` | `/api/model-telemetry/records` | 记录一次模型调用遥测 | 请求体包含 review、strategy、provider、model、role、promptVersion、status、latency、token、cost、error |
| `GET` | `/api/model-telemetry/summary` | 查询模型调用汇总 | 返回总调用、失败数、失败率、总 token、估算成本、平均成本、平均延迟、策略维度调用表现，以及按 review finding 人工状态聚合的确认/误报代理、策略命中、跨模型命中、模型覆盖和 Judge 健康指标 |

当前阶段状态：

- `model_call_telemetry` 表已落地，用于承接后续真实模型调用链路的耗时、token、成本和失败信息。
- AI Gateway 页面优先读取 `/api/model-telemetry/summary` 展示调用统计、总失败率、平均成本和策略效果；策略卡片展示调用量、token、平均延迟、平均成本、失败率、确认/驳回/待处理 Finding 数、确认率、误报代理率、策略命中率、跨模型命中率、模型覆盖数和 Judge 失败率；若新接口失败，仍保留旧 `/api/gateway/stats` 兜底。
- AI Gateway 页面已提供手动遥测记录面板，可调用 `POST /api/model-telemetry/records` 写入策略、Provider、模型、状态、延迟、token 和成本，并在成功后刷新 `/api/model-telemetry/summary`。后续真实 Review / Agent 调用链路仍可在服务层使用 `ModelTelemetryRecorder.recordCall(...)` 自动记录。

## 后续 API 演进优先级

1. **Pre-PR Gate 后端化**
   - `POST /api/reviews/pre-pr`
   - 已落地：`GET /api/reviews/{id}/gate`
   - 已落地：`POST /api/reviews/{id}/gate/refresh`
   - 已落地：`POST /api/reviews/{id}/gate/publish-ci`
   - 已落地：`PATCH /api/reviews/{id}/gate/decision`

2. **CI 与代码扫描集成**
   - 已落地：GitHub Commit Status 回写。
   - 已落地：CI 回写配置、provider-aware 状态发布、最近回写日志、失败记录手动重试、到期自动重试、Jenkins 参数模板、Jenkins 队列/构建结果手动与自动刷新，以及 GitHub/GitLab/Jenkins 集成健康度汇总；治理中心已提供三类连接器配置、健康展示和异常行动项入口。
- 已落地：GitHub webhook 签名校验、GitLab webhook token 校验、delivery 幂等、GitLab MR revision 级并发去重、失败触发自动/手动重试、死信终态、队列健康度和投递日志。自动重试默认每分钟扫描到期任务，失败后按指数退避并封顶 30 分钟；累计 5 次仍失败时进入 `EXHAUSTED` 并停止自动消耗，可通过治理页人工重试。扫描间隔可通过 `review-agent.webhook-review.retry-delay-ms` 调整。
   - 已落地：SARIF 导出和 GitHub Code Scanning 上传入口。
   - 已落地：PR Summary 对话区评论回写入口。

3. **策略效果指标**
   - 已落地：模型调用耗时、失败率、token、成本的遥测表、记录接口、汇总接口和 AI Gateway 汇总视图。
   - 已落地：按策略关联 review finding 人工状态，汇总有效 Finding 数、确认率和误报代理指标。
   - 已落地：策略命中率、跨模型命中率、模型覆盖数和 Judge 调用失败率代理指标。
   - 待推进：Judge 分歧的真实裁决差异、跨模型 Finding 归因明细。

4. **修复闭环**
   - Finding 生成 Fix Draft。
   - 人工确认后应用补丁或创建外部 Issue。
