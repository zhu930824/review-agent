# Phase 3 & Phase 4 执行计划

## 当前状态分析

| 阶段 | 已完成 | 待实现 |
|------|--------|--------|
| Phase 1 (10/10) | 多模型Review, RuleEngine, Dashboard, Jenkins/GitLab/GitHub Webhook, Pre-PR, Findings, Judge, GateResult | — |
| Phase 2 (9/9) | GovernanceCenter, RulePack, WorkflowTemplate, TeamMemory, SARIF, SecurityScan, ArchitectureRule, CIStatusCheck, SOPCompiler | — |
| Phase 3 (2/7) | WorkflowEngine, AutoFixDraft | ReleaseWorkflow, AITestGeneration, RefactorWorkflow, JiraIntegration, ChangeRiskPrediction |
| Phase 4 (4/7) | MCPToolSystem, OrganizationMemory, ArchitectureBrain, KnowledgeGraph | AIReleaseManager, AIRefactorPlanner, AIArchitectureCommittee |

---

## 一、Phase 3：AI Workflow Platform（4~8个月）

### 1.1 Release Workflow（发布工作流）

**目标：** 将 Pre-PR Gate → 审查 → 人工确认 → 合并 → 发布 串联为可执行工作流

**实施步骤：**
1. 新建 `infrastructure/workflow/ReleaseWorkflowService.java` — 发布工作流编排服务
2. 新建 `infrastructure/workflow/WorkflowStep.java` — 工作流步骤定义（门禁/审查/确认/合并/发布）
3. 新建 `controller/ReleaseController.java` — `POST /api/releases` 启动发布流程
4. 在 SimpleWorkflowEngine 中注册 Release 工作流模板

### 1.2 AI Test Generation（AI 测试用例生成）

**目标：** AI 根据 diff 变更自动生成测试用例

**实施步骤：**
1. 新建 `infrastructure/testgen/TestCaseGenerationService.java` — 测试用例生成服务
2. 新建 `infrastructure/testgen/TestCase.java` — 测试用例模型（接口/方法/输入/期望输出）
3. 新建 `infrastructure/testgen/TestCoveragePlan.java` — 测试覆盖计划（按 SOP 五步法）
4. 新建 `controller/TestGenController.java` — `POST /api/reviews/{id}/generate-tests`
5. 在 AiGateway 中新增 test-generation Prompt 模板

### 1.3 Refactor Workflow（重构工作流）

**目标：** AI 根据技术债发现生成重构计划

**实施步骤：**
1. 新建 `infrastructure/refactor/RefactorPlan.java` — 重构计划模型
2. 新建 `infrastructure/refactor/RefactorPlannerService.java` — 重构规划服务（基于 Findings + Rules + Memory 生成重构步骤）
3. 新建 `controller/RefactorController.java` — `POST /api/reviews/{id}/refactor-plan`

### 1.4 Jira Integration（Jira 集成）

**目标：** 将 Review Findings 自动同步为 Jira 工单

**实施步骤：**
1. 新建 `infrastructure/integration/JiraConnector.java` — Jira API 连接器
2. 新建 `infrastructure/integration/IssueTracker.java` — 工单同步接口（Jira/Linear 统一抽象）
3. 新建 `infrastructure/integration/IssueSyncService.java` — 发现→工单转换服务
4. 新增到 `GovernanceController` 或独立 `IntegrationController` — `POST /api/integration/sync/{source}`

### 1.5 Change Risk Prediction（变更风险预测）

**目标：** 基于历史 Findings + 知识图谱预测 PR 风险等级

**实施步骤：**
1. 新建 `infrastructure/risk/ChangeRiskPredictor.java` — 风险预测引擎（基于文件路径/变更量/历史Bug密度/规则命中率）
2. 新建 `infrastructure/risk/RiskAssessment.java` — 风险评估模型（文件级+PR级）
3. 在 ReviewController 中新增 `GET /api/reviews/{id}/risk` 端点
4. 在 Pre-PR 流程中集成风险预评估

---

## 二、Phase 4：Enterprise AI Engineering Platform（8~12个月）

### 2.1 AI Release Manager（AI 发布管理 Agent）

**目标：** Agent 自主执行发布检查清单（SOP 驱动）

**实施步骤：**
1. 新建 `infrastructure/agent/role/ReleaseManagerAgent.java` — 发布管理 Agent
2. 在 ReleaseWorkflowService 中集成 Agent 决策能力
3. 新增发布检查清单 SOP 模板
4. 在 WorkflowTemplate 中新增 release-gate 模板

### 2.2 AI Refactor Planner（AI 重构规划 Agent）

**目标：** Agent 基于技术债全景图制定分阶段重构计划

**实施步骤：**
1. 新建 `infrastructure/agent/role/RefactorPlannerAgent.java` — 重构规划 Agent
2. 在 RefactorPlannerService 中集成 Agent 推理能力
3. 新增重构优先级评估（影响面 × 风险 × 业务价值）
4. 在 KnowledgeController 中新增 `GET /api/knowledge/refactor-plan` 端点

### 2.3 AI Architecture Committee（AI 架构委员会）

**目标：** 多 Agent 模拟架构评审委员会，对重大变更进行多方评估

**实施步骤：**
1. 新建 `infrastructure/agent/role/ArchitectureCommitteeOrchestrator.java` — 架构委员会编排器
2. 定义委员会角色：架构师 Agent、安全 Agent、性能 Agent、DBA Agent
3. 委员会投票与结论汇总机制（多数/一致/否决）
4. 在 ArchitectureBrain 中集成委员会决策能力
5. 新增 `POST /api/knowledge/committee/review` 端点

---

## 三、前端配套更新

### 3.1 新增前端纯函数测试（SOP 要求先写测试）

1. `tests/releaseWorkflow.test.ts` — 发布工作流逻辑测试
2. `tests/testGeneration.test.ts` — 测试生成逻辑测试
3. `tests/refactorPlan.test.ts` — 重构计划逻辑测试
4. `tests/changeRisk.test.ts` — 变更风险计算逻辑测试

### 3.2 新增前端工具函数

1. `utils/releaseWorkflow.ts` — 发布工作流状态机
2. `utils/testGeneration.ts` — 测试用例渲染和分组
3. `utils/refactorPlan.ts` — 重构步骤优先级排序
4. `utils/changeRisk.ts` — 风险评分计算

### 3.3 新增前端类型定义

1. `types/release.ts` — 发布工作流类型
2. `types/testgen.ts` — 测试生成类型
3. `types/refactor.ts` — 重构规划类型
4. `types/risk.ts` — 风险评估类型

---

## 四、执行顺序（依赖关系）

```
Phase 3（可并行）
├── 1.2 AI Test Generation ────────────── 独立
├── 1.3 Refactor Workflow ─────────────── 独立
├── 1.4 Jira Integration ──────────────── 独立
├── 1.5 Change Risk Prediction ────────── 需要 KnowledgeGraphEngine
└── 1.1 Release Workflow ──────────────── 需要 WorkflowEngine + Pre-PR Gate

Phase 4（依赖 Phase 3 部分成果）
├── 2.1 AI Release Manager ─────────────── 需要 ReleaseWorkflow + Agent Runtime
├── 2.2 AI Refactor Planner ────────────── 需要 RefactorWorkflow + Agent Runtime
└── 2.3 AI Architecture Committee ──────── 需要 ArchitectureBrain + Agent Runtime
```

---

## 五、质量保障（SOP 要求）

- 新增前端纯函数 → 先写测试，再实现
- 每次前端页面改动后 → `npm test` + `npm run build`
- 后端源码改动前 → 先确认 JDK21 编译通过
- 数据库迁移 → 只做新增表或新增索引
