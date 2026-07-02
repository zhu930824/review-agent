# Review Agent API 清单

> 更新时间：2026-07-01

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
| `POST` | `/api/auth/logout` | 退出登录 | 当前后端不维护 token 黑名单 |

前端认证存储：

| Key | 用途 |
| --- | --- |
| `review-agent-token` | JWT token |
| `review-agent-user` | 当前用户信息 |

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
| `GET` | `/api/projects/{id}/branches` | 查询项目分支 | Review 创建页使用 |

## Reviews

来源：前端调用确认。对应部分后端源码当前不可稳定文本读取。

| Method | Path | 用途 | 前端使用位置 |
| --- | --- | --- | --- |
| `POST` | `/api/reviews` | 创建普通 Review | 旧入口，后端源码当前受保护 |
| `POST` | `/api/reviews/pre-pr` | 创建 Pre-PR 审查 | `views/reviews/create.vue`；创建成功后前端初始化 Gate |
| `GET` | `/api/reviews` | 分页查询 Review | Dashboard、项目详情 |
| `GET` | `/api/reviews/{id}` | 查询 Review 详情 | Review 详情页 |
| `PATCH` | `/api/reviews/{reviewId}/finding/{findingId}` | 更新 Finding 人工状态 | Review 详情页 |
| `GET` | `/api/reviews/{id}/progress` | SSE 审查进度 | Review 详情页 |
| `GET` | `/api/reviews/{id}/risk` | 获取风险预测 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/generate-tests` | 生成测试覆盖计划 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/refactor-plan` | 生成重构计划 | Review 详情页智能分析 |

### Pre-PR Gate

来源：后端可读源码确认，`PrePrGateController`；前端 Review 详情页已接入。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `GET` | `/api/reviews/{id}/gate` | 查询或初始化后端持久化 Gate 状态 | 优先读取 `pre_pr_gate`，不存在时按 Review/Finding 计算并写入 |
| `POST` | `/api/reviews/{id}/gate/initialize` | 初始化 Pre-PR Gate | Pre-PR 创建成功后调用；写入 `pre_pr_gate_history` 初始化事件；不发布 CI |
| `POST` | `/api/reviews/{id}/gate/refresh` | 重新计算并持久化 Gate 状态 | Finding 人工状态变化后使用；成功后自动发布 CI 状态 |
| `POST` | `/api/reviews/{id}/gate/publish-ci` | 将持久化 Gate 状态发布到 CI/PR 状态系统 | `PASSED` -> success，`BLOCKED` / `NEEDS_HUMAN_REVIEW` -> failure，`RUNNING` -> pending；Review 详情页提供手动重发入口 |
| `PATCH` | `/api/reviews/{id}/pre-pr-decision` | 记录人工 Gate 决策 | 请求体：`PrePrGateDecisionRequest`；写入 `pre_pr_gate_history` 决策事件；成功后自动发布 CI 状态 |

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

- `POST /api/reviews/pre-pr` 已由前端创建页使用，后端源码当前受保护，按调用契约记录。
- Pre-PR 创建成功后，前端调用 `POST /api/reviews/{id}/gate/initialize`，确保进入详情页前已有持久化 Gate 和初始化历史。
- GitHub Status 回写已读取持久化 Gate；GitHub Checks API / GitLab 回写仍属于下一阶段。

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

## Governance

来源：后端可读源码确认，`GovernanceController`。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/governance/capabilities` | 查询治理能力目录 |
| `GET` | `/api/governance/connectors` | 查询集成连接器路线图 |
| `GET` | `/api/governance/rule-packs` | 查询治理规则包 |
| `GET` | `/api/governance/workflows` | 查询工作流模板 |

## Operations

来源：后端可读源码确认，`OperationsController`；前端运营中心读取 Operations 聚合接口形成策略压力视图。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/operations/dashboard` | 查询运营中心数据 |
| `GET` | `/api/operations/strategy-pressure` | 查询策略成本/质量压力排行 | 基于模型遥测 summary 生成压力分、压力等级和运营建议 |
| `GET` | `/api/operations/remediation-queue` | 查询运营中心修复队列 | 参数：`limit`，默认 50；返回未驳回 Finding，包含 Review、项目、严重度、人工状态、置信度和跨模型命中信息 |
| `GET` | `/api/operations/owner-load` | 查询运营责任人负载 | 返回按 Finding 分类映射的责任人、数量和占比 |
| `GET` | `/api/operations/rule-learning-candidates` | 查询规则学习候选 | 参数：`limit`，默认 20；返回 `PROMOTE_TO_RULE` / `SUPPRESS_PATTERN` 候选 |
| `GET` | `/api/operations/business-impact` | 查询业务收益估算 | 基于近期 Finding 的 review 数、人工确认/驳回覆盖率、BLOCKER/MAJOR 数量估算节省审查时间和规避返工时间 |
| `GET` | `/api/operations/telemetry-readiness` | 查询模型遥测接入就绪度 | 基于模型遥测 summary 输出策略级 `READY` / `NEEDS_ATTRIBUTION` / `JUDGE_UNSTABLE` / `NOT_CONNECTED` 状态和建议 |

当前前端接入状态：

- 运营中心全局 KPI 优先读取 `/api/operations/dashboard`，修复队列读取 `/api/operations/remediation-queue`，责任人负载读取 `/api/operations/owner-load`，规则学习视图读取 `/api/operations/rule-learning-candidates`，业务收益估算读取 `/api/operations/business-impact`；责任人负载、规则学习候选和业务收益均保留本地推导兜底。
- 运营中心“策略成本/质量压力”面板调用 `/api/operations/strategy-pressure` 和 `/api/operations/telemetry-readiness`，按失败率、误报代理、确认率、策略命中率、Judge 失败率、平均成本和延迟展示后端生成的策略压力分、压力等级、运营建议和遥测接入/归因就绪状态。
- 治理中心“遥测行动项”面板调用 `/api/operations/telemetry-readiness`，将 `NO_TELEMETRY`、`WEAK_ATTRIBUTION`、`JUDGE_FAILURE` 等 `gapCode` 转成平台集成待办，便于从治理视角继续补齐模型调用遥测、跨模型归因和 Judge 稳定性。

## Integration

来源：后端可读源码确认，`CiStatusConfigController`；前端治理中心已接入。

| Method | Path | 用途 | 备注 |
| --- | --- | --- | --- |
| `GET` | `/api/integration/ci-config` | 查询 CI 回写配置 | 默认 `connectorKey=github-checks` |
| `PUT` | `/api/integration/ci-config` | 保存 CI 回写配置 | token 和 webhook secret 不在响应中明文返回 |
| `GET` | `/api/integration/ci-config/writebacks` | 查询最近 CI 回写记录 | 参数：`limit`，最大 50 |
| `POST` | `/api/integration/ci-config/writebacks/{id}/retry` | 手动重试失败回写 | 仅允许 `FAILED` 记录；重发当前持久化 Gate 状态；到期 `nextRetryAt` 记录也会由后端调度自动重试 |
| `POST` | `/api/integration/webhooks/github` | 接收 GitHub webhook 投递 | 校验 `X-Hub-Signature-256`；使用 `X-GitHub-Delivery` 做幂等；写入投递日志 |
| `POST` | `/api/integration/sarif/upload` | 上传 SARIF 到 GitHub Code Scanning | 读取 GitHub 集成配置；请求体包含 `commitSha`、`ref`、`sarif` |
| `POST` | `/api/integration/pr-summary/comment` | 回写 PR Summary 评论 | 使用 GitHub Issues comments API；请求体包含 `pullNumber`、`body` |
| `GET` | `/api/integration/actions` | 查询最近集成动作日志 | 参数：`limit`，最大 50；覆盖 SARIF 上传、PR Summary 评论等外部动作 |

当前前端接入状态：

- Review 详情页保留本地 SARIF 下载，同时新增“一键上传扫描”，会读取当前 Review 的 SARIF、`sourceCommit/targetCommit` 和分支 ref 后调用 `/api/integration/sarif/upload`。
- Review 详情页新增 “PR Summary” 操作，用户输入 Pull Request 编号后，将当前 Review 摘要、Gate 状态、严重度统计和前 5 条 Finding 组合成 Markdown 并调用 `/api/integration/pr-summary/comment`。
- 治理中心 “CI 回写就绪度” 面板新增最近集成动作列表，调用 `/api/integration/actions` 展示 `SARIF_UPLOAD`、`PR_SUMMARY_COMMENT` 的 `UPLOADED`、`POSTED`、`SKIPPED`、`FAILED` 状态。

## Knowledge

来源：前端调用确认。

| Method | Path | 用途 | 前端使用位置 |
| --- | --- | --- | --- |
| `GET` | `/api/knowledge/query?keyword={keyword}` | 查询知识节点 | `views/knowledge.vue` |

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
- 受保护 Gateway / Review 主流程当前不直接修改；后续可在可维护调用点直接接入 `POST /api/model-telemetry/records`，或在服务层使用 `ModelTelemetryRecorder.recordCall(...)` 包装真实模型调用，自动记录成功/失败、耗时、token 和成本。

## 后续 API 演进优先级

1. **Pre-PR Gate 后端化**
   - `POST /api/reviews/pre-pr`
   - 已落地：`GET /api/reviews/{id}/gate`
   - 已落地：`POST /api/reviews/{id}/gate/refresh`
   - 已落地：`POST /api/reviews/{id}/gate/publish-ci`
   - 已落地：`PATCH /api/reviews/{id}/pre-pr-decision`

2. **CI 与代码扫描集成**
   - 已落地：GitHub Commit Status 回写。
   - 已落地：CI 回写配置、最近回写日志、失败记录手动重试和到期自动重试。
   - 已落地：GitHub webhook 签名校验、delivery 幂等和投递日志。
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
