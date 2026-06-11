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
