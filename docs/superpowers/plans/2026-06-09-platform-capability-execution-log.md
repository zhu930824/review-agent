# Review Agent 平台能力补齐执行日志

> 对应计划：`2026-06-09-platform-capability-roadmap.md`

## 2026-06-09

### 已完成切片

1. API base 统一
   - `useAuth` 改为复用 `getApiBaseUrl()`，避免 Auth 请求固定写死 `/api`。
   - 新增测试覆盖 Auth 与共享 API base 的一致性。

2. Pre-PR 创建链路接入后端专用接口
   - 创建页改为调用 `POST /reviews/pre-pr`。
   - 按后端 `ReviewDetailVO` 的嵌套响应读取 `res.data.review.id` 后跳转详情页。
   - 新增 `ReviewDetailResponse` 前端类型。

3. SARIF 导出入口
   - 详情页增加 SARIF 下载按钮。
   - 调用 `GET /reviews/{id}/sarif`，并以 `application/sarif+json` 生成 `.sarif` 文件下载。
   - 新增测试覆盖导出端点、Blob 类型与文件后缀。

4. Gate 决策主数据源适配
   - `deriveGateStatus` 优先使用后端持久化的 `prePrStatus`。
   - 将后端人工通过态 `APPROVED` 归一为前端可展示的 `PASSED`。
   - `generateBlockedReasons` 优先使用后端持久化的 `blockedReasons`，避免详情页重复拼接前端推导原因。
   - 新增测试覆盖持久化 Gate 决策和阻断原因优先级。

5. 人工 Gate 决策入口
   - 详情页阻断原因卡片增加“人工放行”和“维持阻断”操作。
   - 调用后端 `PATCH /reviews/{id}/pre-pr-decision`，提交 `{ decision, comment }`。
   - 人工决策提交后使用后端返回的 `ReviewDetail` 刷新页面状态。
   - 新增测试覆盖决策接口调用、人工放行动作和后端响应刷新。

6. PR/CI 回写就绪度面板
   - 治理能力目录增加 `getCiStatusIntegrationReadiness()`。
   - 明确 GitHub Checks / Status 依赖 `ci-status-check`、`quality-gate`、`sarif-export`。
   - 记录后端已存在的 CI 回写信号：`reportRunning`、`reportPass`、`reportBlock`。
   - 治理中心增加“CI 回写就绪度”面板，展示依赖能力、后端信号和下一步动作。
   - 新增测试覆盖 catalog 契约和治理页展示。

7. PR/CI 回写配置与仓库绑定
   - 后端新增 `integration_ci_config` 表，用于保存 GitHub Checks / Status 的仓库绑定和回写配置。
   - 后端新增 `GET /api/integration/ci-config` 和 `PUT /api/integration/ci-config`。
   - 后端响应通过 `tokenConfigured`、`webhookSecretConfigured` 表示密钥是否已配置，不返回 `apiToken` 或 `webhookSecret` 明文。
   - 治理中心 CI 回写面板增加配置表单：Repo Owner、Repo Name、Status Context、API Token、Webhook Secret、Checks 开关、SARIF 上传开关。
   - 新增测试覆盖后端契约、密钥脱敏边界和前端配置表单。

8. GitHub Status 回写服务
   - 新增 `GitHubStatusRequestFactory`，负责构造 GitHub Status API URL、Header 和请求体。
   - 新增 `GitHubCiStatusService`，以 `@Primary` 方式成为 `CiStatusService` 的优先实现。
   - `reportRunning`、`reportPass`、`reportBlock` 分别映射到 GitHub Status 的 `pending`、`success`、`failure`。
   - 服务从 `integration_ci_config` 读取 GitHub Checks 配置，从 Review 读取 commit SHA；配置不完整、未启用或没有 commit 时安全跳过。
   - 新增后端单元测试覆盖 GitHub Status 请求构造、鉴权头、context、target_url 和 description 长度限制。

### 验证记录

- `tsx --test tests/sarifExport.test.ts`：62 pass，0 fail。
- `tsx --test tests/*.test.ts`：62 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `tsx --test tests/reviewMetrics.test.ts`：64 pass，0 fail。
- `tsx --test tests/*.test.ts`：64 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `tsx --test tests/prePrDecision.test.ts`：66 pass，0 fail。
- `tsx --test tests/*.test.ts`：66 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `tsx --test tests/governanceCatalog.test.ts`：67 pass，0 fail。
- `tsx --test tests/ciStatusReadinessView.test.ts`：68 pass，0 fail。
- `tsx --test tests/*.test.ts`：68 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `tsx --test tests/ciStatusConfigBackend.test.ts tests/ciStatusConfigView.test.ts`：73 pass，0 fail。
- `tsx --test tests/*.test.ts`：73 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `mvn test`：后端构建成功；当前没有后端测试源码，执行到 surefire 后 `BUILD SUCCESS`。
- `mvn -Dtest=GitHubStatusRequestFactoryTest test`：2 pass，0 fail。
- `mvn test`：后端 2 个单元测试通过，`BUILD SUCCESS`。

### 下一步建议

1. 增加回写失败记录、重试状态和最近一次同步状态展示。
2. 补 GitHub Checks API 模式或 SARIF 上传任务。
3. 补模型调用遥测与策略效果看板的最小可用数据结构。
