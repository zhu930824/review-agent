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

9. CI 回写可观测性与失败记录
   - 后端新增 `ci_status_writeback_log` 表，记录 review、commit SHA、回写 state、请求 URL、状态、错误信息、重试次数和下一次重试时间。
   - 新增 `CiStatusWritebackLogService`，在 GitHub Status 回写成功、失败和跳过时写入审计记录。
   - CI 回写服务对审计记录写入做异常隔离，避免日志记录失败影响状态回写主链路。
   - `GET /api/integration/ci-config/writebacks` 提供最近回写记录，默认返回 10 条，最大限制 50 条。
   - 治理中心 CI 回写面板新增“最近回写”列表，展示 SUCCESS / FAILED / SKIPPED、retry 次数、commit 和错误原因。
   - 新增前端契约测试覆盖后端日志契约、失败/重试字段和治理页展示接入。

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
- `tsx --test tests/ciWritebackObservability.test.ts`：76 pass，0 fail。
- `tsx --test tests/*.test.ts`：76 pass，0 fail。
- `vite build`：构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `mvn test`：后端 2 个单元测试通过，`BUILD SUCCESS`。

### 下一步建议

1. 补 GitHub Checks API 模式或 SARIF 上传任务。
2. 补模型调用遥测与策略效果看板的最小可用数据结构。
3. 增加 CI 回写失败自动重试调度与手动重试入口。

## 2026-06-11

### 已完成切片

1. 后端持久化 Pre-PR Gate 服务
   - 新增 `PrePrGateService` / `PrePrGateServiceImpl`，将 Gate 状态从前端推导推进为后端领域能力。
   - 新增 `PrePrGateRepository` 抽象和 MyBatis 实现，复用既有 `pre_pr_gate`、`review`、`review_finding` 表。
   - Gate 计算规则：
     - Review 为 `PENDING` 或 `RUNNING` 时返回 `RUNNING`。
     - 存在 `BLOCKER` Finding 时返回 `BLOCKED`。
     - 存在待人工确认的 `MAJOR` Finding 时返回 `NEEDS_HUMAN_REVIEW`。
     - 严重问题已处理后返回 `PASSED`。
   - 人工决策通过 `decidedBy`、`decidedAt` 和持久化原因记录审计信息。

2. Pre-PR Gate API
   - 新增 `GET /api/reviews/{id}/gate`，查询或初始化后端持久化 Gate 状态。
   - 新增 `POST /api/reviews/{id}/gate/refresh`，在 Finding 人工状态变化后重新计算 Gate。
   - 新增 `PATCH /api/reviews/{id}/pre-pr-decision`，记录人工 Gate 决策。
   - 新增 `PrePrGateVO`、`PrePrGateDecisionRequest`、`PrePrGateFindingInput` 后端合同。

3. Review 详情页接入后端 Gate
   - Review 详情加载完成后读取 `/reviews/{id}/gate`，优先展示后端持久化的 `gateStatus` 和 `blockedReasons`。
   - Finding 人工状态变更后调用 `/reviews/{id}/gate/refresh`，再刷新 Gate 展示。
   - 人工放行/维持阻断改为提交 `{ gateStatus, reason, decidedBy }` 到后端决策接口。
   - 后端 Gate 接口失败时，页面仍保留原有本地推导兜底。

4. API 文档更新
   - `docs/api-catalog.md` 更新到 2026-06-11。
   - 将 Gate 接口从演进建议升级为已确认接口，并补充请求/响应合同。

### 验证记录

- `mvn -q -Dtest=PrePrGateServiceImplTest test`：5 个 Gate 服务单元测试通过。
- `mvn -q -DskipTests compile`：后端编译通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。
- `npm test`：前端 76 个测试通过。
- `mvn -q test`：后端测试通过。

### 下一步建议

1. 将 `POST /api/reviews/pre-pr` 创建链路与 Gate 初始化联动。
2. 将 GitHub/GitLab 状态回写改为读取后端持久化 Gate 状态。
3. 为 Gate API 增加 Controller 契约测试或集成测试。
4. 将 `decidedBy` 从临时前端值替换为后端登录用户上下文。

## 2026-06-11 续

### 已完成切片

1. 持久化 Gate 到 CI 状态回写映射
   - 新增 `PrePrGateCiStatusPublisher`，统一从 `PrePrGateService.getGate(reviewId)` 读取持久化 Gate。
   - `PASSED` 映射到 `CiStatusService.reportPass`。
   - `BLOCKED` 和 `NEEDS_HUMAN_REVIEW` 映射到 `CiStatusService.reportBlock`，优先使用后端持久化阻断原因作为 description。
   - `RUNNING` 映射到 `CiStatusService.reportRunning`。

2. Gate CI 发布 API
   - 新增 `POST /api/reviews/{id}/gate/publish-ci`。
   - 该接口不重新推导状态，只读取当前后端 Gate 主数据源并发布到现有 CI 状态服务。
   - API 可用于后续 Review 完成事件、人工决策后同步、或运维手动重发。

### 验证记录

- `mvn -q -Dtest=PrePrGateCiStatusPublisherTest test`：4 个 Gate 到 CI 映射测试通过。
- `mvn -q -Dtest=PrePrGateControllerTest test`：Gate CI 发布控制器合同测试通过。
- `mvn -q test`：后端测试通过。
- `npm test`：前端 76 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 在 Review 完成事件或人工决策成功后自动调用 `PrePrGateCiStatusPublisher`。
2. 将 `publish-ci` 接入前端运维/详情页的手动重发按钮。
3. 补 GitHub Checks API 模式或 SARIF 上传任务。

## 2026-06-11 再续

### 已完成切片

1. Gate 关键动作自动发布 CI 状态
   - `POST /api/reviews/{id}/gate/refresh` 在重新计算并持久化 Gate 后，自动调用 `PrePrGateCiStatusPublisher`。
   - `PATCH /api/reviews/{id}/pre-pr-decision` 在记录人工决策后，自动调用 `PrePrGateCiStatusPublisher`。
   - `POST /api/reviews/{id}/gate/publish-ci` 仍保留为手动重发入口。
   - 由于 Review 主流程源码仍受保护，暂不直接改 Review 完成事件；当前可维护触发点已经覆盖 Finding 人工状态变更和人工 Gate 决策。

### 验证记录

- `mvn -q -Dtest=PrePrGateControllerTest test`：3 个控制器合同测试通过，覆盖 refresh 后发布、人工决策后发布、手动发布。
- `mvn -q -Dtest=PrePrGateCiStatusPublisherTest test`：4 个 Gate 到 CI 映射测试通过。
- `mvn -q test`：后端测试通过。
- `npm test`：前端 76 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 在受保护 Review 主流程可维护后，把 Review 完成事件接入 `PrePrGateCiStatusPublisher`。
2. 在 Review 详情页增加“重新发布 CI 状态”按钮，调用 `POST /api/reviews/{id}/gate/publish-ci`。
3. 推进 CI 回写失败自动重试和手动重试入口。

## 2026-06-11 继续

### 已完成切片

1. Review 详情页手动重发 CI 状态
   - 在 Review 详情头部操作区新增“重发 CI”按钮。
   - 点击后调用 `POST /api/reviews/{id}/gate/publish-ci`，由后端读取当前持久化 Gate 并发布到 CI/PR 状态系统。
   - 增加 `ciRepublishLoading`，避免重复点击时没有状态反馈。
   - 该入口用于 CI 配置修正、外部状态写回失败后的人工作业补偿。

2. 前端合同测试
   - 扩展 `tests/prePrDecision.test.ts`，覆盖详情页重发按钮、loading 状态和 publish-ci API 调用。

### 验证记录

- `npx tsx --test tests/prePrDecision.test.ts`：3 个测试通过。
- `mvn -q test`：后端测试通过。
- `npm test`：前端 77 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 在治理中心最近回写列表增加“手动重试”入口，优先针对 `FAILED` / 可重试记录。
2. 后端补 CI 回写失败重试服务，读取 `integration_ci_writeback_log.next_retry_at`。
3. 在 Review 完成事件可维护后，自动调用 Gate 刷新和发布。

## 2026-06-11 阶段 1 收尾

### 已完成切片

1. Pre-PR 创建后显式初始化 Gate
   - 新增 `POST /api/reviews/{id}/gate/initialize`，用于 Pre-PR 创建成功后的 Gate 初始化。
   - 前端创建页在 `POST /api/reviews/pre-pr` 返回 review id 后，先调用 `/reviews/{id}/gate/initialize`，再跳转详情页。
   - 初始化接口只写入持久化 Gate 和历史事件，不触发 CI 发布，避免创建阶段过早写外部状态。

2. Gate 决策历史审计
   - 新增 `pre_pr_gate_history` 表，记录 review、Gate 状态、事件类型、原因、操作者和时间。
   - `PrePrGateService.initializeGate` 追加 `INITIALIZED` 历史事件。
   - `PrePrGateService.decideGate` 追加 `MANUAL_DECISION` 历史事件。

3. 阶段 1 状态同步
   - `docs/api-catalog.md` 将 `POST /api/reviews/pre-pr` 和 `/gate/initialize` 记录为已接入链路。
   - `docs/product-design-and-roadmap.md` 标记阶段 1 退出标准已满足。

### 验证记录

- `mvn -q "-Dtest=PrePrGateServiceImplTest,PrePrGateControllerTest,PrePrGateCiStatusPublisherTest" test`：Gate 初始化、人工决策历史、Controller 和 CI 映射测试通过。
- `npx tsx --test tests/prePrDecision.test.ts`：4 个前端合同测试通过。
- `mvn -q test`：后端测试通过。
- `npm test`：前端 78 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 进入阶段 2：优先补治理中心最近回写列表的手动重试入口和后端重试服务。
2. 推进 Webhook 签名、幂等和投递日志。
3. 推进 SARIF 上传和 PR Summary 回写。

## 2026-06-11 阶段 2 启动

### 已完成切片

1. CI 回写失败手动重试服务
   - 新增 `CiStatusWritebackRetryService`，按 `integration_ci_writeback_log.id` 读取失败记录。
   - 仅允许 `FAILED` 记录重试；非失败记录或缺少 review id 时拒绝重试。
   - 重试时增加原日志 `retryCount`，清空 `nextRetryAt`，再通过 `PrePrGateCiStatusPublisher` 重发当前持久化 Gate 状态。
   - 新增 `CiStatusWritebackRetryRepository`，隔离 MyBatis Plus 的日志读取和更新。

2. 治理中心手动重试入口
   - `POST /api/integration/ci-config/writebacks/{id}/retry` 暴露失败回写重试端点。
   - 治理中心最近回写列表对 `FAILED` 记录显示“重试”按钮。
   - 增加 `ciWritebackRetryingIds` loading 状态，重试完成后刷新最近回写列表。

3. API 文档同步
   - `docs/api-catalog.md` 新增 Integration / CI 配置与回写日志接口清单。

### 验证记录

- `mvn -q "-Dtest=CiStatusWritebackRetryServiceImplTest,CiStatusWritebackRetryControllerTest" test`：后端重试服务和 Controller 合同测试通过。
- `npx tsx --test tests/ciWritebackObservability.test.ts`：4 个前端/合同测试通过。
- `mvn -q test`：后端测试通过。
- `npm test`：前端 79 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 增加到期失败回写的自动重试调度，消费 `next_retry_at`。
2. 推进 Webhook 签名校验、幂等 key 和投递日志。
3. 推进 SARIF 上传到 GitHub Code Scanning。

## 2026-06-12 阶段 2 继续

### 已完成切片

1. CI 回写失败自动重试调度
   - `CiStatusWritebackRetryService` 新增 `retryDueWritebacks()`，批量读取 `FAILED` 且 `nextRetryAt <= now` 的回写记录。
   - 自动重试复用手动重试主链路：增加 `retryCount`、清空 `nextRetryAt`，再通过 `PrePrGateCiStatusPublisher` 重发当前持久化 Gate 状态。
   - `CiStatusWritebackRetryRepository` 新增 `findDueFailedWritebacks(now, limit)`，按 `nextRetryAt` 升序最多消费 20 条。
   - 新增 `CiStatusWritebackRetryScheduler`，默认每 60 秒触发一次；间隔可通过 `review-agent.ci-writeback.retry-delay-ms` 配置。
   - 新增独立 `CiWritebackSchedulingConfig` 启用 Spring Scheduling，避免修改受保护主应用入口。
   - 自动批处理按单条日志隔离异常；缺少 review id 等不可重试记录会跳过，不阻断后续到期记录。

2. API 文档同步
   - `docs/api-catalog.md` 标记 CI 回写失败记录已支持手动重试和到期自动重试。

### 验证记录

- `mvn -q "-Dtest=CiStatusWritebackRetryServiceImplTest,CiStatusWritebackRetrySchedulerTest,CiStatusWritebackRetryControllerTest" test`：自动重试服务、调度委托和手动重试 Controller 合同测试通过。
- `mvn -q test`：后端测试通过；自动重试批处理中不可重试记录会输出预期 warn 并继续。
- `npx tsx --test tests/ciWritebackObservability.test.ts`：4 个治理中心 CI 回写合同测试通过。
- `npm test`：前端 79 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 推进 Webhook 签名校验、幂等 key 和投递日志。
2. 推进 SARIF 上传到 GitHub Code Scanning。
3. 为自动重试补充运维侧开关、批次指标和失败告警。

## 2026-06-12 阶段 2 Webhook 强化

### 已完成切片

1. GitHub Webhook 可信接入
   - 新增 `POST /api/integration/webhooks/github`，读取 `X-GitHub-Delivery`、`X-GitHub-Event` 和 `X-Hub-Signature-256`。
   - 新增 `GitHubWebhookSignatureVerifier`，按 GitHub `sha256=` HMAC-SHA256 规则校验请求体签名。
   - 复用 `integration_ci_config.webhook_secret` 作为签名密钥来源，保持密钥仍由治理中心 CI 配置维护。

2. 幂等与投递日志
   - 新增 `integration_webhook_delivery_log` 表，记录 connector、provider、delivery id、事件类型、状态、签名、payload digest、错误信息和处理时间。
   - 新增 `IntegrationWebhookDeliveryService`，对重复 `deliveryId` 返回 `DUPLICATE`，避免重复处理同一次外部投递。
   - 签名失败会写入 `REJECTED` 投递日志并拒绝请求；签名通过写入 `ACCEPTED`。
   - 新增可维护的 `IntegrationWebhookController`，避免直接修改受保护旧 `WebhookController` / handler 源码。

3. API 文档同步
   - `docs/api-catalog.md` 记录 GitHub webhook 接收端点和签名、幂等、投递日志约束。

### 验证记录

- `mvn -q "-Dtest=GitHubWebhookSignatureVerifierTest,IntegrationWebhookDeliveryServiceImplTest,IntegrationWebhookControllerTest" test`：签名校验、幂等投递日志和 Controller 合同测试通过。

### 下一步建议

1. 推进 SARIF 上传到 GitHub Code Scanning。
2. 将 webhook ACCEPTED 事件接入更细的业务处理器，例如 PR opened/synchronize 后触发 Review/Gate 刷新。
3. 为 webhook 投递日志补治理中心查询视图和失败重放入口。

## 2026-06-12 阶段 2 SARIF 上传

### 已完成切片

1. GitHub Code Scanning SARIF 上传入口
   - 新增 `POST /api/integration/sarif/upload`，请求体包含 `commitSha`、`ref` 和 SARIF JSON 字符串。
   - 新增 `GitHubSarifUploadRequestFactory`，按 GitHub Code Scanning REST API 构造 `POST /repos/{owner}/{repo}/code-scanning/sarifs` 请求。
   - SARIF 内容按 GitHub 要求先 gzip，再 Base64 编码后写入请求体 `sarif` 字段。
   - 请求体包含 `commit_sha`、`ref`、`tool_name=Review Agent` 和 `validate=true`。
   - 新增 `GitHubSarifUploadService`，读取 `github-checks` 集成配置；仅在 `sarifUploadEnabled=true` 且 GitHub 仓库和 token 配置完整时上传。
   - 上传 client 独立为 `GitHubSarifUploadClient`，测试中不访问真实 GitHub。

2. 受保护源码隔离
   - 现有 Review SARIF 导出链路仍在受保护源码附近，本轮不直接改动。
   - 新入口可供现有 SARIF 导出结果、后续后台任务或详情页操作复用。

3. API 文档同步
   - `docs/api-catalog.md` 将 SARIF 导出与 GitHub Code Scanning 上传入口标记为已落地。

### 验证记录

- `mvn -q "-Dtest=GitHubSarifUploadRequestFactoryTest,GitHubSarifUploadServiceImplTest,IntegrationSarifControllerTest" test`：SARIF 上传请求构造、配置跳过和 Controller 合同测试通过。

### 下一步建议

1. 将 Review 详情页的 SARIF 导出结果串接到 `/api/integration/sarif/upload`，形成一键上传操作。
2. 补 SARIF 上传日志和治理中心最近上传记录。
3. 推进 PR Summary 回写。

## 2026-06-12 阶段 2 PR Summary 回写

### 已完成切片

1. GitHub PR Summary 评论入口
   - 新增 `POST /api/integration/pr-summary/comment`，请求体包含 `pullNumber` 和 Markdown `body`。
   - 新增 `GitHubPrSummaryCommentRequestFactory`，按 GitHub 一般 PR 对话评论合同构造 `POST /repos/{owner}/{repo}/issues/{pullNumber}/comments` 请求。
   - 使用治理中心 `github-checks` 集成配置中的 GitHub 仓库和 token。
   - 新增 `GitHubPrSummaryCommentClient` 隔离真实 GitHub 网络调用，便于后续接入日志、重试或 mock。

2. 接口边界
   - 本轮实现的是 PR 对话区 summary comment，不是代码行级 review comment。
   - 未直接改动受保护 Review 主流程；后续可由 Review 完成事件、Webhook 事件或详情页操作调用该入口。

3. API 文档同步
   - `docs/api-catalog.md` 记录 PR Summary 评论回写入口，并注明使用 GitHub Issues comments API。

### 验证记录

- `mvn -q "-Dtest=GitHubPrSummaryCommentRequestFactoryTest,GitHubPrSummaryCommentServiceImplTest,IntegrationPrSummaryControllerTest" test`：PR Summary 请求构造、配置跳过和 Controller 合同测试通过。

### 下一步建议

1. 将 Review 完成事件或详情页操作串接到 PR Summary 评论入口。
2. 为 PR Summary 回写增加日志、幂等 marker 和失败重试。
3. 进入阶段 3：模型调用遥测与策略效果看板的最小数据结构。

## 2026-07-01 阶段 2 使用体验与集成闭环补齐

### 已完成切片

1. Review 详情页串接 SARIF 上传
   - 在 Review 详情头部操作区新增“上传扫描”按钮。
   - 点击后先读取既有 `GET /api/reviews/{id}/sarif` 导出结果，再调用 `POST /api/integration/sarif/upload` 上传到 GitHub Code Scanning。
   - 上传请求自动使用当前 Review 的 `sourceCommit/targetCommit` 和源/目标分支 ref，减少用户手工填集成参数。
   - 增加 `sarifUploadLoading` 与成功/失败反馈，避免集成动作没有可见状态。

2. Review 详情页串接 PR Summary 评论
   - 在 Review 详情头部操作区新增 “PR Summary” 按钮。
   - 用户输入 Pull Request 编号后，页面将当前 Review 摘要、Gate 状态、严重度统计和前 5 条 Finding 组合为 Markdown。
   - 提交后调用 `POST /api/integration/pr-summary/comment`，把 Review 结论写入 GitHub PR 对话区。
   - 增加输入校验、loading 状态和成功/失败反馈。

3. API 文档同步
   - `docs/api-catalog.md` 更新到 2026-07-01。
   - 补充 Review 详情页对 SARIF 上传和 PR Summary 回写的前端接入状态。

4. SARIF / PR Summary 集成动作审计
   - 新增 `integration_action_log` 表，用统一日志记录外部集成动作。
   - SARIF 上传会记录 `SARIF_UPLOAD` 的 `UPLOADED`、`SKIPPED`、`FAILED` 状态，包含 commit、请求 URL 和错误原因。
   - PR Summary 评论会记录 `PR_SUMMARY_COMMENT` 的 `POSTED`、`SKIPPED`、`FAILED` 状态，包含 PR 编号、请求 URL 和错误原因。
   - GitHub 外部调用异常不再直接向上抛出，服务会返回 `FAILED` 结果并落审计日志。

5. 最近集成动作查询与治理中心视图
   - 新增 `GET /api/integration/actions`，默认返回最近 10 条、最大 50 条集成动作日志。
   - 治理中心 CI 回写就绪度面板新增“最近集成动作”，展示 SARIF 上传和 PR Summary 评论的动作类型、目标、状态和错误信息。

### 验证记录

- `npx tsx --test tests/sarifExport.test.ts`：4 个 Review 详情页 SARIF/PR Summary 合同测试通过。
- `mvn -q "-Dtest=IntegrationActionLogControllerTest,IntegrationActionLogServiceImplTest,GitHubSarifUploadServiceImplTest,GitHubPrSummaryCommentServiceImplTest" test`：集成动作日志查询、SARIF 上传审计和 PR Summary 审计测试通过。
- `npx tsx --test tests/ciWritebackObservability.test.ts`：6 个 CI 回写与集成动作可观测性合同测试通过。
- `npm test`：前端 81 个测试通过。
- `npm run build`：前端构建成功；仍存在 Ant Design Vue chunk 超过 500 kB 的既有体积警告。

### 下一步建议

1. 为 SARIF 上传和 PR Summary 回写增加幂等 marker 和失败重试。
2. 为 webhook 投递日志补治理中心查询视图和失败重放入口。
3. 进入阶段 3：模型调用遥测与策略效果看板的最小数据结构。

## 2026-07-01 阶段 3 启动：模型调用遥测最小闭环

### 已完成切片

1. 模型调用遥测数据结构
   - 新增 `model_call_telemetry` 表，记录 review、strategy、provider、model、role、promptVersion、status、latency、token、成本和错误信息。
   - 新增 `ModelCallTelemetry` 实体、MyBatis mapper 和 repository。
   - 由于既有 Gateway / ModelCallRecord 源码处于受保护区域，本轮通过新的可维护边界落地，不直接改受保护文件。

2. 遥测记录与汇总 API
   - 新增 `POST /api/model-telemetry/records`，用于后续真实模型调用链路写入遥测。
   - 新增 `GET /api/model-telemetry/summary`，返回总调用、失败数、总 token、估算成本、平均延迟和策略维度汇总。
   - 策略维度按调用量降序展示，便于看板优先看到主要策略表现。

3. AI Gateway 策略效果视图
   - AI Gateway 页面优先读取 `/api/model-telemetry/summary` 作为调用统计主数据源。
   - 调用统计新增策略效果列表，展示每个策略的调用量、token、平均延迟和失败率。
   - 新接口失败时保留旧 `/api/gateway/stats` 兜底，避免受保护 Gateway 旧接口不可用时影响页面基础展示。

4. API 文档同步
   - `docs/api-catalog.md` 新增 Model Telemetry API 清单。
   - 将策略效果指标从“待推进”更新为已具备遥测地基，人工确认率、策略命中、Judge 分歧仍留作后续。

### 验证记录

- `mvn -q "-Dtest=ModelTelemetryControllerTest,ModelTelemetryServiceImplTest" test`：模型遥测记录、汇总和 Controller 合同测试通过。
- `npx tsx --test tests/modelTelemetry.test.ts`：2 个后端合同与 AI Gateway 视图接入测试通过。

### 下一步建议

1. 将真实 Review / Agent 模型调用链路逐步接入 `POST /api/model-telemetry/records` 或服务层记录口。
2. 在策略效果汇总里加入人工确认率、有效 Finding 数和误报代理指标。
3. 将模型遥测与 Operations 的风险/治理队列串联，形成“哪个策略更准、更贵、更慢”的运营视角。

## 2026-07-01 阶段 3 继续：策略效果成本与失败率

### 已完成切片

1. 模型遥测汇总增强
   - `GET /api/model-telemetry/summary` 新增总失败率 `failureRatePercent`。
   - 新增平均单次成本 `avgCostMicroCents`。
   - 策略维度新增 `failureRatePercent` 和 `avgCostMicroCents`，前端不再重复计算失败率口径。

2. AI Gateway 策略效果展示增强
   - 调用统计卡片从“失败次数”升级为展示总失败率，并保留失败次数作为辅助指标。
   - 新增平均成本指标，使用微分成本格式化为美元展示。
   - 策略效果列表新增策略级平均成本，并直接使用后端返回的策略级失败率。

3. API 文档同步
   - `docs/api-catalog.md` 补充模型遥测 summary 的失败率和成本字段说明。

### 验证记录

- `mvn -q "-Dtest=ModelTelemetryServiceImplTest" test`：模型遥测失败率和平均成本聚合测试通过。
- `npx tsx --test tests/modelTelemetry.test.ts`：AI Gateway 模型遥测视图合同测试通过。

### 下一步建议

1. 将真实模型调用记录接入可维护服务边界。
2. 继续补策略有效性指标：有效 Finding 数、人工确认率、误报代理指标。
3. 在 Operations 中增加策略成本/质量压力视角。

## 2026-07-01 阶段 3 继续：策略质量结果指标

### 已完成切片

1. 策略效果汇总接入 Review Finding 人工状态
   - `GET /api/model-telemetry/summary` 在策略维度新增 `confirmedFindings`、`dismissedFindings`、`pendingFindings`。
   - 汇总逻辑按策略下的去重 `reviewId` 关联 `review_finding`，避免同一 review 多次模型调用导致 Finding 重复计数。
   - 新增 `confirmationRatePercent`：`CONFIRMED / (CONFIRMED + DISMISSED + PENDING)`。
   - 新增 `falsePositiveProxyPercent`：`DISMISSED / (CONFIRMED + DISMISSED)`，把未处理项排除在误报代理指标之外。

2. 受保护主流程隔离
   - 本轮只在 `ModelTelemetryRepository` 增加旁路查询 `listFindingsByReviewIds`，复用已有 `ReviewFindingMapper`。
   - 不改 Review / Finding 主流程写入逻辑，真实调用链后续可逐步把 `reviewId` 和 `strategyKey` 写入遥测记录。

3. AI Gateway 策略效果视图补齐
   - 策略卡片新增确认、驳回、待处理 Finding 数。
   - 策略卡片新增确认率和误报代理率，和调用量、成本、失败率放在同一视图里，便于比较“准不准、贵不贵、稳不稳”。

4. API 文档同步
   - `docs/api-catalog.md` 补充 Model Telemetry summary 的策略质量字段和当前阶段状态。

### 验证记录

- `mvn -q -Dtest=ModelTelemetryServiceImplTest test`：策略成本、失败率和 Finding 人工状态聚合测试通过。
- `npx tsx --test tests/modelTelemetry.test.ts`：AI Gateway 模型遥测视图合同测试通过。

### 下一步建议

1. 将真实 Review / Agent 模型调用链路接入 `model_call_telemetry`，让质量指标从手动记录过渡到自动采集。
2. 在 Operations 中增加策略成本/质量压力视角。
3. 继续补策略命中、Judge 分歧和跨模型命中指标。

## 2026-07-01 阶段 3 继续：模型调用遥测服务层接入边界

### 已完成切片

1. 模型调用遥测包装器
   - 新增 `ModelTelemetryRecorder`，用于包装真实模型调用的 `Callable`。
   - 成功时自动记录 `SUCCESS`、耗时、上下文和 usage extractor 提取的 token / 成本。
   - 失败时自动记录 `FAILED`、耗时和错误信息，并原样抛回调用异常，不吞掉主链路错误。

2. 可维护上下文 DTO
   - 新增 `ModelTelemetryContext`，承载 `reviewId`、`strategyKey`、`provider`、`modelName`、`role`、`promptVersion`。
   - 新增 `ModelTelemetryUsage`，承载 `promptTokens`、`completionTokens`、`costMicroCents`。
   - 后续 Gateway / Agent / Review worker 只需要在可维护调用点构造上下文并包一层 `recordCall(...)`，即可进入统一遥测表。

3. 受保护主流程隔离
   - 本轮不直接修改受保护 Review / Gateway 主流程源码。
   - 先提供服务层 seam，让后续真实调用链路接入时不需要绕 HTTP，也不需要散落重复记录逻辑。

4. API 文档同步
   - `docs/api-catalog.md` 补充 `ModelTelemetryRecorder.recordCall(...)` 作为服务层接入方式。

### 验证记录

- `mvn -q -Dtest=ModelTelemetryRecorderTest test`：成功调用记录、失败调用记录和异常原样抛回测试通过。

### 下一步建议

1. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
2. 在 Operations 中增加策略成本/质量压力视角。
3. 继续补策略命中、Judge 分歧和跨模型命中指标。

## 2026-07-01 阶段 3 下一阶段：Operations 策略成本/质量压力视图

### 已完成切片

1. 策略压力计算工具
   - 新增 `deriveStrategyPressureItems`，基于策略失败率、误报代理、确认率、平均成本和平均延迟生成 `pressureScore`。
   - 按压力分降序排序，输出 `HIGH` / `MEDIUM` / `LOW` 压力等级。
   - 为高压策略生成降噪、复核成本、缩小适用场景等运营建议。

2. Operations 页面接入模型遥测
   - 运营中心新增“策略成本/质量压力”面板。
   - 页面调用 `/api/model-telemetry/summary`，复用当前模型遥测 summary，不新增后端接口。
   - 面板展示策略压力分、确认率、误报代理、失败率、平均成本、平均延迟和建议。
   - 暂无遥测数据时展示空态提示，真实模型调用接入后自动出现策略压力排行。

3. API 文档同步
   - `docs/api-catalog.md` 补充 Operations 前端读取模型遥测 summary 的当前接入状态。

### 验证记录

- `npx tsx --test tests/reviewOperations.test.ts tests/operationsTelemetry.test.ts`：策略压力排序和 Operations 遥测视图合同测试通过。

### 下一步建议

1. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
2. 继续补策略命中、Judge 分歧和跨模型命中指标。
3. 将策略压力结果升级为后端 Operations dashboard 字段，替换前端本地推导。

## 2026-07-01 阶段 3 继续补齐：策略诊断指标

### 已完成切片

1. Model Telemetry summary 增加策略诊断字段
   - 策略维度新增 `reviewedReviews`、`totalFindings`、`strategyHitRatePercent`、`findingsPerReview`。
   - 策略维度新增 `crossHitFindings`、`crossHitRatePercent`，复用 `review_finding.is_cross_hit` 作为跨模型命中代理指标。
   - 策略维度新增 `modelDiversity`，按 telemetry 中的不同 `modelName` 统计模型覆盖数。
   - 策略维度新增 `judgeCalls`、`judgeFailureRatePercent`，按 telemetry role=`JUDGE` 统计 Judge 调用健康度。

2. AI Gateway 策略效果视图补齐
   - 策略卡片新增命中率、跨模型命中率、模型覆盖数和 Judge 失败率。
   - 保留原调用量、成本、失败率、确认率、误报代理指标，形成更完整的策略画像。

3. Operations 策略压力视图增强
   - `deriveStrategyPressureItems` 将策略命中率和 Judge 失败率纳入压力分。
   - Operations 策略压力面板新增命中率、跨模型命中率和 Judge 失败率展示。

4. API 文档同步
   - `docs/api-catalog.md` 补充策略命中、跨模型命中、模型覆盖和 Judge 健康指标。

### 验证记录

- `mvn -q -Dtest=ModelTelemetryServiceImplTest test`：策略命中、跨模型命中、模型覆盖和 Judge 健康指标聚合测试通过。
- `npx tsx --test tests/modelTelemetry.test.ts tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：AI Gateway、Operations 和策略压力工具合同测试通过。

### 下一步建议

1. 将策略压力结果升级为后端 Operations dashboard 字段，替换前端本地推导。
2. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
3. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。

## 2026-07-01 阶段 3 继续：Operations 策略压力后端化

### 已完成切片

1. 策略压力后端服务
   - 新增 `OperationsStrategyPressureService` 和 `OperationsStrategyPressureServiceImpl`。
   - 复用 `ModelTelemetryService.summary()` 的策略维度指标，在后端生成 `pressureScore`、`pressureLevel` 和运营建议。
   - 压力分纳入失败率、误报代理、低确认率、成本、延迟、Judge 失败率和低命中率，按压力分降序返回。

2. Operations API 合同
   - 新增 `GET /api/operations/strategy-pressure`。
   - 返回 `OperationsStrategyPressureVO` 列表，包含策略成本、质量、命中、跨模型和 Judge 健康指标。
   - 保持 `GET /api/operations/dashboard` 不变，避免影响现有运营中心基础数据合同。

3. 前端接入切换
   - Operations 页面从直接读取 `/api/model-telemetry/summary` 改为读取 `/api/operations/strategy-pressure`。
   - 页面保留原策略压力展示体验，只把压力计算和建议生成收敛到后端。
   - `deriveStrategyPressureItems` 仍保留为独立工具函数，便于测试和未来 fallback。

4. API 文档同步
   - `docs/api-catalog.md` 新增 Operations 策略压力接口。
   - 将运营中心当前接入状态更新为读取 Operations 聚合接口。

### 验证记录

- `mvn -q "-Dtest=OperationsStrategyPressureServiceImplTest,OperationsControllerTest" test`：策略压力服务和 Operations Controller 合同测试通过。
- `npx tsx --test tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：Operations 页面新接口接入和策略压力工具测试通过。

### 下一步建议

1. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
2. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。
3. 将运营中心本地样例修复队列逐步替换为真实 Finding / Gate / SLA 聚合数据。

## 2026-07-01 阶段 3 继续：Operations 真实修复队列

### 已完成切片

1. 运营修复队列后端边界
   - 新增 `OperationsRemediationQueueService` 和 `OperationsRemediationQueueServiceImpl`。
   - 新增 `OperationsRemediationQueueRepository` 和 MyBatis 实现，只读 `review_finding`、`review`、`project`，不改 Review 主流程。
   - 队列排除 `DISMISSED` Finding，并按严重度、待人工处理、跨模型命中和 Finding ID 排序。

2. Operations API 合同
   - 新增 `GET /api/operations/remediation-queue`，支持 `limit` 参数，默认 50，上限 100。
   - 返回 `OperationFindingVO` 列表，包含 Finding、Review、项目、严重度、分类、人工状态、置信度和跨模型命中信息。

3. 前端接入切换
   - Operations 页面从本地样例 Finding 切换到 `/api/operations/remediation-queue?limit=20`。
   - 修复队列、SLA 压力、责任人负载、规则学习候选和运营就绪度改为跟真实 Finding 返回联动。
   - 业务收益估算暂时保留本地运营假设，后续可接入真实 Review 吞吐和缺陷闭环数据。

4. API 文档同步
   - `docs/api-catalog.md` 新增 Operations 修复队列接口。
   - 将运营中心当前接入状态更新为真实 Finding 驱动。

### 验证记录

- `mvn -q "-Dtest=OperationsRemediationQueueServiceImplTest,OperationsControllerTest" test`：修复队列 service 排序/限量和 Controller 合同测试通过。
- `npx tsx --test tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：Operations 页面真实队列接入和队列工具测试通过。

### 下一步建议

1. 将 Operations dashboard 基础 KPI 改为后端真实聚合，减少前端重复计算。
2. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
3. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。

## 2026-07-01 阶段 3 继续：Operations dashboard KPI 接入

### 已完成切片

1. 前端 KPI 数据源切换
   - Operations 页面新增读取 `GET /api/operations/dashboard`。
   - 开放风险项、BLOCKER 数、待人工确认数和 SLA 压力优先使用后端 dashboard 的全局聚合。
   - 当 dashboard 读取失败时，保留基于当前修复队列的本地推导兜底，避免页面空白。

2. 队列和 KPI 职责拆分
   - 修复队列表格继续读取 `/api/operations/remediation-queue?limit=20`，用于展示当前优先处理项。
   - 全局 KPI 不再依赖前 20 条队列推导，避免有限列表导致总数和 SLA 压力失真。
   - 责任人负载和规则学习仍基于当前队列展示，保留运营执行视角。

3. API 文档同步
   - `docs/api-catalog.md` 更新 Operations 当前接入状态，明确 dashboard 和 remediation queue 的职责分工。

### 验证记录

- `npx tsx --test tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：Operations 页面 dashboard / queue 接入合同和队列工具测试通过。
- `mvn -q "-Dtest=OperationsRemediationQueueServiceImplTest,OperationsControllerTest" test`：Operations 后端队列与 Controller 合同测试通过。

### 下一步建议

1. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
2. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。
3. 将责任人负载和规则学习候选也逐步后端聚合化。

## 2026-07-01 阶段 3 继续：Operations 责任人负载后端化

### 已完成切片

1. 责任人负载后端聚合
   - 新增 `OperationOwnerLoadVO`。
   - `OperationsRemediationQueueService` 新增 `listOwnerLoad()`，基于 open Finding 聚合责任人负载。
   - 责任人映射与前端既有口径保持一致：Security Owner、Performance Owner、Tech Lead、Code Owner。

2. Operations API 合同
   - 新增 `GET /api/operations/owner-load`。
   - 返回责任人、未关闭 Finding 数量和占比，按数量降序展示。
   - 聚合排除 `DISMISSED` Finding，避免已驳回项继续占用执行负载。

3. 前端接入切换
   - Operations 页面新增读取 `/api/operations/owner-load`。
   - 责任人负载面板优先使用后端 owner-load；接口失败时保留基于当前修复队列的本地推导兜底。
   - 修复队列和规则学习候选仍保持现有展示逻辑。

4. API 文档同步
   - `docs/api-catalog.md` 新增 owner-load 接口，并更新运营中心接入状态。

### 验证记录

- `mvn -q "-Dtest=OperationsRemediationQueueServiceImplTest,OperationsControllerTest" test`：owner-load 聚合和 Controller 合同测试通过。
- `npx tsx --test tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：Operations 页面 owner-load 接入和队列工具测试通过。

### 下一步建议

1. 将规则学习候选后端聚合化，减少前端对 Finding 规则沉淀口径的重复维护。
2. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
3. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。

## 2026-07-01 阶段 3 继续：Operations 规则学习候选后端化

### 已完成切片

1. 规则学习候选后端聚合
   - 新增 `OperationRuleLearningCandidateVO`。
   - `OperationsRemediationQueueService` 新增 `listRuleLearningCandidates(limit)`。
   - 候选口径覆盖两类运营动作：人工确认且跨模型命中/高置信的 Finding 生成 `PROMOTE_TO_RULE`；人工驳回且低置信的 Finding 生成 `SUPPRESS_PATTERN`。

2. 仓储读取口径补齐
   - `OperationsRemediationQueueRepository` 新增 `listFindings(limit)`，用于规则学习读取已驳回样本。
   - `listOpenFindings(limit)` 继续排除 `DISMISSED`，保持修复队列和责任人负载只面向未关闭风险。

3. Operations API 合同
   - 新增 `GET /api/operations/rule-learning-candidates`。
   - 支持 `limit` 参数，默认 20，上限沿用服务层 100。
   - 返回 finding id、动作、规则标题和沉淀原因。

4. 前端接入切换
   - Operations 页面新增读取 `/api/operations/rule-learning-candidates?limit=20`。
   - 规则学习面板优先使用后端候选；接口失败时保留基于当前修复队列的本地推导兜底。

5. API 文档同步
   - `docs/api-catalog.md` 新增 rule-learning-candidates 接口，并更新运营中心接入状态。

### 验证记录

- `mvn -q "-Dtest=OperationsRemediationQueueServiceImplTest,OperationsControllerTest" test`：规则学习候选聚合和 Controller 合同测试通过。
- `npx tsx --test tests/operationsTelemetry.test.ts tests/reviewOperations.test.ts`：Operations 页面规则学习候选接入和队列工具测试通过。

### 下一步建议

1. 在可读的 Gateway / Agent 模型调用实现中接入 `ModelTelemetryRecorder`。
2. 补 Judge 分歧的真实裁决差异和跨模型 Finding 归因明细。
3. 将运营中心业务收益估算逐步后端化，减少前端固定假设。
