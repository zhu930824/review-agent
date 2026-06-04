# Review Agent 优化演进方案

## 背景

当前项目已经从一个 AI Code Review 工具，推进到了 AI Coding 研发治理平台的雏形。它具备前后端分离应用、数据库迁移、模型策略、Pre-PR 审查、治理中心、运营看板、知识管理、AI 网关和修复草案等模块。

本方案的目标不是继续堆功能，而是让平台从“能跑、能展示”演进为“可信、可嵌入交付链路、可持续治理”。

## 当前已实现能力

### 基础业务闭环

- 项目管理：登记 Git 仓库、维护默认分支、查看克隆状态。
- Review 创建：支持选择项目、源分支、目标分支和审查策略。
- Review 详情：展示状态、模型执行结果、Finding 列表、人工确认/忽略。
- 数据库表：已有 `project`、`review`、`review_finding`、`review_model_result`。

### 模型与策略治理

- 已有 `model_provider`、`model_profile`、`review_strategy`、`review_strategy_model` 数据表。
- 后端已有 `ModelConfigController` 和 `ModelConfigServiceImpl`，支持模型供应商、模型档案、审查策略和角色绑定的管理。
- 前端 `/settings/models` 已能展示供应商、模型档案和审查策略摘要。
- 前端工具函数已能把内置策略编译为后端兼容的 `modelsConfig`。

### Pre-PR 质量门禁

- 前端创建页支持 Pre-PR 审查入口。
- Review 详情页已能基于 BLOCKER、MAJOR 和人工状态推导 Gate 状态。
- `reviewMetrics.ts` 已提供 Dashboard 指标、Gate 状态、阻断原因和模型成功率计算。
- 数据库已有 `pre_pr_gate` 表，为后端真实门禁状态持久化预留空间。

### 治理中心和运营视角

- 数据表已有治理能力目录、集成连接器、规则包和工作流模板。
- 前端已有 Dashboard、Governance、Operations、Knowledge、Gateway 等页面。
- 测试已覆盖模型策略、治理目录、运营指标、风险预测、测试生成、重构计划等纯函数。

### 验证现状

截至 2026-06-03 的本地验证结果：

- `review-agent-web`: `npm test` 通过，79 个测试通过。
- `review-agent-web`: `npm run build` 通过，Vue、Ant Design Vue 和 review utils 已拆分 chunk；当前剩余大包警告集中在 Ant Design Vue vendor chunk。
- `review-agent-server`: `mvn -q -DskipTests compile` 通过。

### 第一阶段已完成项

- 登录态 token key 已统一为 `review-agent-token`，并由 `authStorage.ts` 集中管理。
- API base URL 已集中到 `apiConfig.ts`，普通 API 和 Review SSE 进度都复用同一配置入口。
- 路由守卫已抽为可测试函数，覆盖未登录跳转、已登录访问业务页和访客页回首页。
- Review 创建 payload 编译已抽为可测试工具函数，覆盖模型名映射、Gate policy、override 配置和提交 payload。
- README、API 清单和开发环境说明已与当前 Vite + Vue 3 + Ant Design Vue 项目状态对齐。
- Vite 已配置 `vue-vendor`、`ant-design-vue`、`review-utils` 手工拆包。
- 第二阶段已开始执行：新增 `GET /api/reviews/{id}/gate`，前端创建页切到 `/api/reviews/pre-pr`，Review 详情页接入 Gate 状态、人工决策、SARIF 下载、Pre-PR Markdown 报告复制和下载。
- 第三阶段已开始执行：运营中心从硬编码样例切换为后端 `/api/operations/dashboard` 数据源，修复队列、SLA、规则学习和业务收益估算基于真实 Finding 聚合；同时接入 `/api/gateway/stats`，新增模型调用健康、失败率、平均耗时、Token 与成本估算看板；运营聚合数据已带出 `reviewMode/modelsConfig`，前端可按 `strategyId` 展示策略效果、确认率、误报倾向和交叉命中率。

## 主要短板

### 产品可信度

- 核心业务页面和 Pre-PR 报告已新增可读文案回归测试；更广范围的注释、历史文档和终端编码显示仍需后续审计。
- 前端 UI 已新增 Playwright E2E，覆盖登录态重定向、Pre-PR Gate 展示、人工门禁决策、SARIF 导出和 Pre-PR 报告下载；后续仍需接入真实后端环境和外部平台回写验证。

### 后端源码资产风险

- 部分 Java 文件可正常读取，部分 Java 文件读取为 `Esafenet` 保护内容或二进制内容。
- 虽然后端可以编译，但核心审查编排、AI 引擎等文件暂时不适合盲目重构。
- 后续应先确认源码资产可维护性，再推进核心后端架构改造。

### Pre-PR 闭环不完整

- 后端已有 `pre_pr_gate` 表、`PrePrGate` 实体、`POST /api/reviews/pre-pr`、`PATCH /api/reviews/{id}/pre-pr-decision` 和 `GET /api/reviews/{id}/sarif` 雏形。
- Review 详情已接入独立 `GET /api/reviews/{id}/gate` 契约，并保留 `ReviewDetailVO.prePrStatus` 作为兼容数据。
- Pre-PR 创建页已切换到 `/api/reviews/pre-pr` 专用接口，后端 `CreatePrePrRequest` 已兼容可选 `reviewMode` 和 `modelsConfig`。
- 后端已具备 provider-neutral CI status payload 边界、GitHub/GitLab payload 转换、Commit Status endpoint 动态装配和可配置 HTTP publisher；GitHub/GitLab 外部平台验证和真实分支保护链路仍待接入。
- SARIF 导出后端接口、前端下载入口和浏览器级下载验证已存在，后续需要代码扫描平台上传能力。
- 后端已提供 `GET /api/reviews/{id}/pre-pr-report` 统一生成 Pre-PR Markdown 报告，并提供默认关闭的 `POST /api/reviews/{id}/pre-pr-report/publish` HTTP 发布边界；Review 详情页已接入复制和下载，真实 PR 摘要评论写回和 Issue 同步仍处在规划或半实现状态。

### 策略效果缺少反馈

- 模型调用成功率、成本、耗时已经接入运营中心看板；策略维度确认率、误报倾向和交叉命中率已接入第一版运营看板，后续需要沉淀到后端历史指标表。
- 人工确认/忽略结果已经在运营中心转化为规则学习候选，后续需要沉淀为可保存、可审批的规则包变更。
- 治理中心更接近能力展示，还未成为规则运营和策略改进工作台。

## 推荐演进路线

建议按“先修稳，再闭环，再智能化”的顺序推进。

### 第一阶段：稳定化和产品可信度

周期：1-2 周。

目标：让平台从“能跑”变成“可信可演示”。

重点任务：

- 修复登录态 token key 不一致。
- 更新 README，让技术栈、启动方式、API 代理和验证命令与真实项目一致。
- 逐步修复核心页面中文文案编码问题。
- 梳理 API 清单：Auth、Project、Review、ModelConfig、Governance、Operations。
- 增加认证存储、路由守卫、Review 创建等基础测试。
- 优化 Vite 拆包，降低主 chunk 体积。

### 第二阶段：Pre-PR 真实闭环

周期：2-4 周。

目标：让 Review Agent 进入真实研发交付链路。

重点任务：

- 巩固已有 `pre_pr_gate` 服务逻辑，让 Gate 状态具备独立读取契约和稳定前端展示。
- 明确 `POST /api/reviews/pre-pr`、新增 `GET /api/reviews/{id}/gate`、继续使用 `PATCH /api/reviews/{id}/pre-pr-decision`。
- 前端创建页按 Pre-PR 场景调用专用接口，并保留策略配置的可演进入口。
- 接入 GitHub/GitLab status check HTTP 写回，让 BLOCKED/PASSED 回写到 PR；当前已完成 provider-neutral payload 边界、provider-specific payload 转换、Commit Status endpoint 动态装配和可配置 HTTP publisher。
- 前端补齐 SARIF 下载入口，把 Finding 接入代码扫描生态。
- Review 详情页支持生成、复制、导出 PR 摘要和审查报告；当前已补后端统一 Markdown 报告接口和默认关闭的 HTTP 发布边界，后续补真实外部平台写回。

### 第三阶段：策略效果与治理运营

周期：4-6 周。

目标：从“审查工具”升级为“治理平台”。

重点任务：

- 沉淀模型调用指标：耗时、失败率、token、成本已进入运营中心；命中数量和长期历史趋势仍需补齐。
- 增加策略效果看板：策略阻断风险、确认率、误报倾向和交叉命中率已进入运营中心；规则命中和 Judge 分歧仍需补齐。
- 把人工确认结果转化为规则学习候选。
- Operations 页面升级为治理工作台：待确认队列、SLA、owner、规则学习候选、业务影响估算、模型调用健康和策略效果已接入后端运营聚合数据。

### 第四阶段：Agent 化和修复闭环

周期：6-10 周。

目标：从“发现问题”推进到“可控修复”。

重点任务：

- 标准化 Fix Draft：Finding 可生成修复建议、补丁草案和测试建议。
- 引入人审工作流：AI 生成草案，人确认后应用。
- Refactor Planner 从展示型能力升级为任务拆解与执行建议。
- MCP 工具链接入 Agent Pipeline：读文件、Git log、结构分析、保存 Finding、外部 Issue。
- 针对安全、性能、异常处理、架构边界建立专项 Agent role 和规则包。

## 优先策略

推荐采用稳健产品化路径：

1. 先修复登录态、文档不一致、编码和验证体系。
2. 再推进 Pre-PR Gate 的后端持久化和 CI 状态回写。
3. 最后把策略效果、规则学习、修复草案和 Agent 工具链串成闭环。

这个路径风险最低，也最符合当前项目状态：平台方向已经清楚，下一步最值钱的是让 Pre-PR Gate 真正进入交付链路，而不是继续堆展示型页面。

## 下一份执行计划

第二阶段执行计划已拆到 `docs/superpowers/plans/2026-06-03-pre-pr-closed-loop.md`。该计划优先做四件事：

1. 纠正并冻结 Pre-PR API 契约。
2. 增加 `GET /api/reviews/{id}/gate` 独立读取接口。
3. 前端 Review 详情页接入后端 Gate 状态、人工决策和 SARIF 下载。
4. 增加轻量 CI status adapter，为 GitHub/GitLab 回写做可替换边界。
