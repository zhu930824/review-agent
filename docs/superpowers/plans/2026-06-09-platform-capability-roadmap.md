# Review Agent 平台能力缺口与演进计划节点

> **给后续执行者：** 本文是平台级路线规划，不是可以直接逐步执行的代码实施清单。进入任一节点前，应再为该节点拆出独立实施计划，保存到 `docs/superpowers/plans/`，并按测试、实现、验证的节奏推进。

**目标：** 基于 2026-06-09 的最新代码，梳理平台已实现能力、当前不足、需要补齐的关键能力，以及后续演进节点。

**总体架构方向：** 保持现有 Vite + Vue 3 前端和 Spring Boot 后端。把 Review、Pre-PR Gate、治理中心、运营中心、AI Gateway、Agent Pipeline 拆成边界清晰的能力域，通过明确 API、持久化状态、事件记录和审计日志协作。

**评估日期：** 2026-06-09

---

## 1. 当前结论

Review Agent 已经不是一个单点 AI Code Review Demo。项目里已经具备项目管理、仓库克隆、Review 创建、模型策略配置、治理能力目录、运营视图、登录鉴权，以及规则、Skill、MCP、Memory、SARIF、Fix Draft、风险预测、测试生成、发布工作流、重构计划等 AI 工程治理概念。

主要问题不在“方向不清楚”，而在“能力成熟度不均衡”。有些能力已经有后端 CRUD 和数据表；有些能力主要是前端 helper 和看板推导；有些能力只是源码结构或概念预留；还有一部分核心 Java 文件在当前工作区读取为 `Esafenet` 保护内容，无法可靠审计。下一阶段应该优先让核心链路可信、可审计、可集成，而不是继续堆更多展示型页面。

## 2. 已实现能力盘点

### 2.1 项目与仓库管理

- 后端已提供项目创建、列表、详情、更新、删除、重试克隆、分支列表等接口。
- `ProjectServiceImpl` 会持久化项目元数据，并调用 `GitService` 执行仓库克隆和分支读取。
- 前端已有项目列表、项目详情、项目创建、项目卡片跳转和从项目发起 Review 的入口。
- 数据库 `project` 表已包含仓库地址、默认分支、本地路径、状态和时间字段。

### 2.2 Review 与 Pre-PR 入口

- 前端 Review 创建页支持选择项目、源分支、目标分支和审查策略。
- Review 详情页支持展示状态、模型执行结果、Finding、人工确认状态、智能分析、风险、测试计划和重构计划入口。
- 数据库已有 `review`、`review_finding`、`review_model_result`、`pre_pr_gate` 表。
- 前端 `reviewMetrics.ts` 已能基于 BLOCKER、MAJOR、人工状态和模型成功率推导 Gate 状态。
- Review 详情页的 SSE 进度连接已通过共享 API base helper 生成地址。

### 2.3 多模型与策略配置

- 后端可读代码中，模型配置接口已经覆盖 Provider、Profile、Review Strategy 和策略角色绑定。
- 数据库迁移已定义 `model_provider`、`model_profile`、`review_strategy`、`review_strategy_model`。
- 种子数据包含 DashScope、OpenAI Compatible、自定义供应商，以及 fast-scan、cross-check、quality-gate、architecture-board 等策略。
- 前端 `modelStrategies.ts` 能把内置策略编译为 Review 创建需要的模型配置，并提供 Gate policy 和角色标签。

### 2.4 治理能力目录

- 数据库已定义治理能力、集成连接器、治理规则包和工作流模板。
- 前端已有治理中心页面，并有测试覆盖能力覆盖率、推荐动作、连接器分组、规则包编译、工作流模板映射等逻辑。
- 当前治理目录已经覆盖 PR 摘要、Inline Review、质量门禁、安全扫描、SARIF 导出、CI 状态回写等市场常见能力。

### 2.5 运营与指标

- 前端已有 Dashboard 和 Operations 页面。
- 前端 helper 已覆盖修复队列优先级、SLA 压力、Owner 工作量、规则学习候选、运营记分卡、业务影响估算、吞吐、质量压力、Gate 状态、阻断原因和模型成功率。
- 后端已有 `/api/operations/dashboard` 入口，但更深层的运营工作流成熟度仍需要继续验证。

### 2.6 登录鉴权与应用外壳

- 后端已有注册、登录、登出接口。
- JWT、AuthInterceptor、密码哈希、用户账户表、认证 DTO 已存在。
- 前端已经抽出 `authStorage.ts`，路由守卫也使用共享 token key。
- 应用页面已覆盖登录、注册、Dashboard、项目、Review、治理、运营、知识、网关和模型设置。

### 2.7 Agent 化基础部件

仓库中已经出现以下 Agent 平台部件：

- Agent Pipeline 配置、Agent 角色、Role Template、Review Orchestrator、Agent Factory。
- Java 规范、安全、性能、异常处理等 Skill。
- 读文件、Git log、代码结构分析、保存 Finding 等工具。
- MCP bridge、MCP client/server config。
- Team Memory 与知识图谱相关概念。
- SARIF 转换、Fix Draft 生成、风险预测、测试生成、重构计划、发布工作流、Webhook 分发、CI 状态服务、Issue Tracker 集成。

这些都是平台化的重要积木，但其中多个 Java 文件在当前工作区无法可靠读取源码，所以只能确认“结构存在”，不能完全确认“行为完整且可维护”。

## 3. 当前不足与需要补齐的能力

### 3.1 核心 Review 链路还不是可信交付门禁

平台已有 Pre-PR 概念，但 Gate 状态还没有明确由后端域服务统一负责。前端能推导 Gate，数据库也有 `pre_pr_gate` 表，但以下关键接口仍处于待落地或未稳定确认状态：

- `POST /api/reviews/pre-pr`
- `GET /api/reviews/{id}/gate`
- `PATCH /api/reviews/{id}/pre-pr-decision`

如果 Gate 决策不能由后端持久化、审计并回写外部系统，平台就仍是“Review 助手”，还不是“合并准入系统”。

### 3.2 工程集成闭环偏浅

平台需要进入团队真实做决策的系统里：

- GitHub/GitLab Checks 或 Commit Status 回写。
- PR 评论和 Review 摘要回写。
- SARIF 导出和上传。
- Jira、Linear 或其他 Issue Tracker 同步。
- CI 结果回收，并与 AI Finding 关联。
- Webhook 签名校验、幂等处理和失败重试。

当前这些能力在数据目录、源码结构或文档里已有雏形，但还没有形成完整用户链路。

### 3.3 治理中心更像目录，还不是治理操作系统

治理能力、规则包、工作流模板已经存在，但缺少生命周期：

- 规则创建、版本、发布、回滚、Owner。
- 规则命中历史和抑制历史。
- 人工确认结果转化为规则学习候选。
- 按团队、仓库、模型、策略、规则包统计效果。
- 带 Owner、SLA、状态流转和审计的治理工作队列。

没有这些闭环，治理中心会更像一张能力地图，而不是每天可用的治理工作台。

### 3.4 AI Gateway 缺少可观测与控制面

模型供应商、模型档案和策略配置已经有基础，但生产级 AI Gateway 还需要：

- 每次调用的耗时、失败、token、成本、重试次数、模型、策略、角色、Prompt 版本记录。
- 按团队、仓库、策略、供应商设置预算和限额。
- Prompt 与响应内容的保留策略。
- 模型降级、重试和熔断。
- 误报率、人工确认率、Judge 一致性等评估数据。

这些能力决定多模型 Review 能否规模化，而不是只在 Demo 中显得强。

### 3.5 Agent Pipeline 缺少执行边界

项目已经有 Agent、Skill、Tool、Memory、MCP 概念，但还需要明确执行契约：

- 每个 Agent role 能调用哪些工具。
- 每个 role 能看到哪些上下文。
- 工具调用如何记录、回放和审计。
- 多 Agent Finding 如何去重、合并和归因。
- Judge 如何覆盖或调和 Worker 结果。
- 人工确认如何进入 Memory、规则候选或策略调整。

这是“Prompt 编排”和“可靠工程 Agent 平台”的分界线。

### 3.6 受保护源码影响产品可信度和后续重构

多个核心 Java 文件读取时显示为 `Esafenet` 保护内容或二进制样式内容。后端可以编译，但源码审计和安全重构受阻。

这应被视为一级工程风险：

- Review 和 Agent 核心行为无法可靠审查。
- API 目录无法完全从源码确认。
- 针对受保护模块的测试设计会变弱。
- 新协作者无法理解核心实现。

深度后端演进前，应先恢复可维护源码，或把受保护模块隔离到稳定接口后面，并补黑盒测试。

### 3.7 测试覆盖前端偏多，后端和端到端不足

前端已有不少纯函数测试，覆盖模型策略、治理目录、指标、运营、auth storage、API config、Vite config 等。后端目前主要能确认编译通过，但还缺少：

- Service 单元测试。
- Controller 契约测试。
- Flyway schema 与 MyBatis mapper 持久化测试。
- 项目克隆、Review 创建、Gate 决策、模型策略加载的集成测试。
- 登录、创建项目、创建 Review、进度订阅、Gate 决策的端到端测试。

当前测试更像“业务 helper 可靠性验证”，还不是“平台工作流可靠性验证”。

### 3.8 安全、租户、审计还未产品级

登录鉴权已经有基础，但治理平台需要更强控制：

- 超出默认角色的 RBAC。
- 组织、团队或租户模型。
- 模型配置、策略、规则、Gate 决策、人工确认的审计日志。
- Token 注销或会话失效。
- 模型 Key、仓库凭据、集成 Token 的密钥管理。
- Webhook 签名校验。
- Agent 工具和 MCP 调用权限边界。

这些能力是跨团队使用前的基本盘。

### 3.9 文档编码和开发体验需要修复

多个 Markdown、SQL 注释和 Java 字符串在终端输出中显示为 mojibake。它会影响演示、协作、API 理解和长期维护。

开发环境文档也记录了本地 Node 运行时异常。项目应统一运行时版本和验证命令，让开发者不依赖临时 workaround。

## 4. 演进原则

1. 先让 Pre-PR Gate 由后端持久化负责，再继续扩展页面。
2. 所有 AI 决策都要可观测、可回放、可审计。
3. 人工确认结果必须进入治理学习闭环。
4. 模型策略、规则包、工作流模板、集成连接器要保持概念分离。
5. 把受保护源码视为深度后端重构前的阻断项。
6. 优先做薄而完整的能力切片，不做大而散的 UI 扩展。
7. 平台价值用阻断风险、减少 Review 时间、降低误报、治理采纳率来衡量。

## 5. 计划节点

### 节点 0：源码与文档可信化

**周期：** 3-5 天

**目标：** 让项目可审计、可复现、可安全演进。

**范围：**

- 盘点受保护 Java 文件，决定恢复源码、隔离接口，或暂不纳入重构范围。
- 修复 README、开发文档、SQL 注释、关键页面文案中的编码问题。
- 统一前端 API base 使用方式，包括 `useAuth.ts` 登录、注册、登出。
- 标准化 Node、Maven、本地启动和验证命令。
- 刷新 API 目录，标记“源码确认”和“前端调用确认”的区别。

**验收：**

- 维护者能按文档跑通前端测试、前端构建、后端编译。
- 核心文档中文可读。
- 受保护源码风险有明确处理决策。
- Auth 与 API base 行为一致。

### 节点 1：后端持久化 Pre-PR Gate

**周期：** 1-2 周

**目标：** 把 Gate 决策从前端推导升级为后端领域能力。

**范围：**

- 新增或确认 Pre-PR Review 创建、Gate 查询、人工决策接口。
- 实现 `PrePrGateService`，统一计算并持久化 `PASSED`、`BLOCKED`、`NEEDS_HUMAN_REVIEW`、`RUNNING`。
- 结构化保存 blocked reasons。
- 记录 `decided_by`、`decided_at`、人工决策原因和状态变更历史。
- Review 详情页改为优先展示后端 Gate 状态。

**验收：**

- Gate 状态刷新后仍然存在。
- 人工决策可持久化、可审计。
- 前端展示后端返回的 Gate 状态和阻断原因。
- 测试覆盖 blocker、major、dismissed、confirmed、manual override 场景。

### 节点 2：PR 与 CI 集成闭环

**周期：** 2-3 周

**目标：** 让 Review Agent 进入真实交付链路。

**范围：**

- 优先实现 GitHub Checks 或 Commit Status 回写。
- 强化 Webhook：签名校验、幂等、投递日志、失败重试。
- 根据 Review 和 Gate 结果生成 PR 摘要。
- 导出 Finding 为 SARIF。
- 增加集成连接器配置：仓库平台、token secret 名称、启用事件、回写策略。

**验收：**

- PR 中能看到 Review Agent 的 passed、blocked、needs-human-review 状态。
- Finding 能导出为 SARIF。
- Webhook 投递可审计、可重试。
- 集成失败能在运营中心看到。

### 节点 3：Review 质量与策略遥测

**周期：** 2-3 周

**目标：** 知道哪些模型、策略、规则和 Agent 角色真正有效。

**范围：**

- 持久化模型调用记录：耗时、token、成本估算、状态、错误、策略、角色、Prompt 版本。
- 记录 Finding 生命周期：创建、确认、忽略、抑制、转 Issue、转规则。
- 增加策略效果看板：误报代理指标、人工确认率、阻断率、模型一致性、Judge 覆盖、成本、耗时。
- 增加模型 fallback、retry 和 circuit breaker 配置。

**验收：**

- 运营视角能回答：哪个策略噪声大、成本高、速度慢、效果好。
- 模型供应商异常不会静默降低 Review 质量。
- 人工反馈能沉淀为可统计数据。

### 节点 4：Rule、Skill、SOP 生命周期

**周期：** 3-4 周

**目标：** 把团队经验变成可执行、可治理、可回滚的规则资产。

**范围：**

- 增加规则编辑、规则包版本、发布、回滚。
- 增加规则测试样例和历史 Finding dry-run。
- 把确认的 Finding 提升为规则学习候选。
- 把误报的 Finding 转为带有效期和 Owner 的抑制候选。
- 连接 SOP section、Rule Pack、Agent Skill 和 Workflow Template。

**验收：**

- 团队可以发布、测试、回滚规则包。
- 规则更新有 Owner、版本和审计记录。
- 人工决策能直接改善后续 Review。

### 节点 5：Agent 执行契约

**周期：** 3-5 周

**目标：** 让 Agent 模式达到可控自动化水平。

**范围：**

- 定义 Agent role 合约：输入上下文、允许工具、输出 schema、最大步数、超时时间。
- 记录每个 Agent step、模型调用、工具调用、保存 Finding。
- 增加多 Worker Finding 去重和合并。
- 记录 Judge 评估理由、置信度和覆盖关系。
- MCP 工具只能通过 allowlist 和策略调用。
- 增加 Agent 执行回放和调试视图。

**验收：**

- Agent 执行可从日志回放。
- 工具访问受角色和策略约束。
- 多 Agent Finding 可去重，Judge 决策可解释。

### 节点 6：Fix Draft 与人工可控修复

**周期：** 3-4 周

**目标：** 从“发现问题”推进到“提出可控修复”。

**范围：**

- 为确认的 Finding 生成 Fix Draft。
- Fix Draft 包含补丁摘要、影响文件、风险级别、建议测试、回滚说明。
- 增加人工审批流程，审批前不应用补丁。
- 接入 Issue Tracker，同步确认但延期处理的问题。
- 可选支持为修复建议创建独立分支。

**验收：**

- Finding 可以生成可审查的修复草稿。
- 修复草稿不会绕过人工确认直接应用。
- 延期问题可以带上下文同步到外部 Issue。

### 节点 7：企业级就绪

**周期：** 4-6 周

**目标：** 支撑多团队稳定使用。

**范围：**

- 增加组织、团队、项目租户模型。
- 增加 admin、maintainer、reviewer、viewer、integration bot 等角色。
- 增加登录、配置、策略、规则、Gate、修复决策的审计日志。
- 增加模型 Key、仓库凭据、集成 Token 的密钥管理抽象。
- 增加 diff、prompt、response、finding 的保留策略。
- 增加部署 profile、健康检查和基础运维指标。

**验收：**

- 不同团队可以隔离配置和数据。
- 敏感操作可审计。
- 密钥不会明文存储或展示。

## 6. 推荐优先级

下一步最有价值的事情不是再加一个页面，而是把核心价值链打硬：

1. **节点 0：** 解决源码和文档可信度。
2. **节点 1：** 让 Pre-PR Gate 后端持久化、可审计。
3. **节点 2：** 把 Gate 结果回写到 PR/CI 系统。
4. **节点 3：** 衡量模型、策略、规则效果。
5. **节点 4：** 把人工决策转成规则演进。
6. **节点 5-6：** 再推进可控 Agent 执行和 Fix Draft。
7. **节点 7：** 在闭环价值验证后补企业级能力。

这个顺序风险最低，也最符合当前项目状态。节点 1 和节点 2 完成后，Review Agent 才真正从独立分析台进入交付链路。

## 7. 近期 Backlog

### P0

- 恢复或隔离受保护的 Review、Agent 核心源码。
- 实现后端持久化 Pre-PR Gate API 和服务。
- 持久化人工 Gate 决策和阻断原因。
- 统一 Auth 与 API composable 的 API base 配置。
- 修复 README 和核心文档编码。

### P1

- GitHub Checks 或 Commit Status 回写。
- SARIF 导出接口和下载入口。
- 模型调用遥测表和服务。
- Auth、Project、Model Config、Gate Service 后端测试。
- 基于持久化 Finding 生命周期的运营队列。

### P2

- Rule Pack 版本化和规则学习工作流。
- Webhook 投递日志与重试。
- PR Summary 回写。
- 确认债务同步到 Issue Tracker。
- Agent 执行日志与回放视图。

### P3

- 多租户组织与团队模型。
- RBAC 与审计日志系统。
- 密钥管理抽象。
- Fix Draft 分支工作流。
- 策略模板库或策略市场。

## 8. 成功指标

- Pre-PR Gate 采纳率：以 Pre-PR 模式创建的 Review 占比。
- Gate 可信度：由后端持久化状态支撑的 Gate 决策占比。
- Review 精准度代理指标：确认 Finding / 确认加忽略 Finding。
- 策略成本效率：单位模型成本产生的确认 Finding 数。
- 决策耗时：Review 开始到 passed、blocked 或人工决策的时间。
- 集成覆盖率：启用 PR 状态回写的仓库占比。
- 治理转化率：确认 Finding 转规则或 Issue 的比例。
- 运营压力：按 Owner 统计的逾期 BLOCKER 和 MAJOR Finding。

## 9. 风险与缓解

| 风险 | 影响 | 缓解 |
| --- | --- | --- |
| 受保护 Java 源码无法审查或重构 | 阻断深度后端演进 | 恢复源码，或用稳定接口和黑盒测试包住受保护模块 |
| Gate 逻辑继续由前端推导 | 决策不可审计 | 将 Gate 计算和人工决策状态迁移到后端 |
| 工作流集成前继续堆页面 | 产品看起来广，但不进入真实交付 | PR/CI 回写和 SARIF 优先于新页面 |
| 多模型 Review 成本高且噪声大 | 团队不信任平台 | 加遥测、预算、策略指标和反馈闭环 |
| Agent 工具缺少边界 | 带来安全和仓库完整性风险 | 增加 allowlist、执行日志、角色合约、人工审批 |
| 文档持续乱码 | 影响演示、协作和维护 | 修复核心文档并加入 UTF-8 校验 |

## 10. 下一份可执行计划建议

下一份可执行实施计划建议聚焦 **节点 1：后端持久化 Pre-PR Gate**。但在开始前，节点 0 中关于受保护源码的处理决策要先明确。

建议实施切片：

1. Gate domain contract 和测试。
2. Gate 持久化服务。
3. Gate API。
4. Review 详情页接入后端 Gate 状态。
5. 人工决策 UX 和审计字段。
6. 验证命令与 API 目录更新。

在受保护源码风险解决前，不建议启动大范围 Agent 后端重构。

