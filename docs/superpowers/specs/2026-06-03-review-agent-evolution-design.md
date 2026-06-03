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

- `review-agent-web`: `npm test` 通过，49 个测试通过。
- `review-agent-web`: `npm run build` 通过，但主 chunk 约 1.5 MB，存在拆包优化空间。
- `review-agent-server`: `mvn -q -DskipTests compile` 通过。

## 主要短板

### 产品可信度

- README 中描述的前端技术栈是 Nuxt 3/Nuxt UI，但真实项目是 Vite、Vue 3、Ant Design Vue。
- 多处中文文案、注释和文档存在 mojibake，影响演示、协作和维护。
- 前端登录态存在 token key 不一致风险：路由守卫读取 `token`，认证模块写入 `review-agent-token`。

### 后端源码资产风险

- 部分 Java 文件可正常读取，部分 Java 文件读取为 `Esafenet` 保护内容或二进制内容。
- 虽然后端可以编译，但核心审查编排、AI 引擎等文件暂时不适合盲目重构。
- 后续应先确认源码资产可维护性，再推进核心后端架构改造。

### Pre-PR 闭环不完整

- 当前 Gate 状态主要由前端推导，`pre_pr_gate` 尚未形成完整后端状态闭环。
- 缺少 GitHub/GitLab Checks 或 Commit Status 回写，暂未进入真实分支保护链路。
- SARIF 导出、PR 摘要写回、Issue 同步等交付链路能力仍处在规划或半实现状态。

### 策略效果缺少反馈

- 模型调用成功率、成本、耗时、误报率、人工确认率没有完整沉淀。
- 人工确认/忽略结果尚未系统性转化为规则学习候选。
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

- 后端落地 `pre_pr_gate` 服务层，让 Gate 状态由后端持久化和返回。
- 明确或新增 `POST /reviews/pre-pr`、`GET /reviews/{id}/gate`、`PATCH /reviews/{id}/pre-pr-decision`。
- 接入 GitHub/GitLab status check，让 BLOCKED/PASSED 回写到 PR。
- 增加 SARIF 导出，把 Finding 接入代码扫描生态。
- Review 详情页支持生成、复制、导出 PR 摘要和审查报告。

### 第三阶段：策略效果与治理运营

周期：4-6 周。

目标：从“审查工具”升级为“治理平台”。

重点任务：

- 沉淀模型调用指标：耗时、失败率、token、成本、命中数量、人工确认率。
- 增加策略效果看板：策略阻断风险、模型误报、规则命中、Judge 分歧。
- 把人工确认结果转化为规则学习候选。
- Operations 页面升级为治理工作台：待确认队列、SLA、owner、规则学习候选、业务影响估算。

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
