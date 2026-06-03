# Review Agent API 清单

> 更新时间：2026-06-03

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
| `POST` | `/api/reviews` | 创建 Review / Pre-PR 审查 | `views/reviews/create.vue` |
| `GET` | `/api/reviews` | 分页查询 Review | Dashboard、项目详情 |
| `GET` | `/api/reviews/{id}` | 查询 Review 详情 | Review 详情页 |
| `PATCH` | `/api/reviews/{reviewId}/finding/{findingId}` | 更新 Finding 人工状态 | Review 详情页 |
| `GET` | `/api/reviews/{id}/progress` | SSE 审查进度 | Review 详情页 |
| `GET` | `/api/reviews/{id}/risk` | 获取风险预测 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/generate-tests` | 生成测试覆盖计划 | Review 详情页智能分析 |
| `POST` | `/api/reviews/{id}/refactor-plan` | 生成重构计划 | Review 详情页智能分析 |

当前演进建议：

- 新增或明确 `POST /api/reviews/pre-pr`，专门表达 Pre-PR 审查创建。
- 新增或明确 `GET /api/reviews/{id}/gate`，由后端返回持久化 Gate 状态。
- 新增或明确 `PATCH /api/reviews/{id}/pre-pr-decision`，记录人工门禁决策。

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
   - `GET /api/reviews/{id}/gate`
   - `PATCH /api/reviews/{id}/pre-pr-decision`

2. **CI 与代码扫描集成**
   - GitHub/GitLab status check 回写。
   - SARIF 导出和上传。

3. **策略效果指标**
   - 模型调用耗时、失败率、成本、人工确认率。
   - 策略命中、Judge 分歧、跨模型命中。

4. **修复闭环**
   - Finding 生成 Fix Draft。
   - 人工确认后应用补丁或创建外部 Issue。
