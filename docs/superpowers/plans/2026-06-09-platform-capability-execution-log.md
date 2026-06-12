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
