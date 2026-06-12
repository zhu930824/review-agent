# Review Agent API 清单

> 更新时间：2026-06-11

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

## Governance

来源：后端可读源码确认，`GovernanceController`。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/governance/capabilities` | 查询治理能力目录 |
| `GET` | `/api/governance/connectors` | 查询集成连接器路线图 |
| `GET` | `/api/governance/rule-packs` | 查询治理规则包 |
| `GET` | `/api/governance/workflows` | 查询工作流模板 |

## Operations

来源：后端可读源码确认，`OperationsController`。

| Method | Path | 用途 |
| --- | --- | --- |
| `GET` | `/api/operations/dashboard` | 查询运营中心数据 |

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
   - 模型调用耗时、失败率、成本、人工确认率。
   - 策略命中、Judge 分歧、跨模型命中。

4. **修复闭环**
   - Finding 生成 Fix Draft。
   - 人工确认后应用补丁或创建外部 Issue。
