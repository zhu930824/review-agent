# 平台核心模块实现计划

## 当前完成度总览

| 模块 | 完成度 | 已实现 | 待实现 |
|------|--------|--------|--------|
| Review Center | 70%（7/10） | PR Summary、DiffViewer、多模型交叉Review、AI Judge、Review Memory | Review Timeline、Review Replay、Diff Context |
| Pre-PR Gate | 100%（7/7步骤） | 全部7步均有代码 | 缺少统一流水线引擎串联 |
| Rule Governance | 87.5%（7/8类型） | Architecture/Performance/Security/SOP/AI Rule/Strategy/Workflow | DDO 规则 |
| Agent Runtime | 8/8 Role，6维均值~50% | 全部8种Agent角色 | Memory/Skill/Rule/Tool/Workflow 补齐 |
| Workflow Engine | 33.3%（2/6） | PR Workflow、Release Workflow | Emergency Fix、Architecture Review、Security Audit、Refactor |
| CI/CD 集成 | 100%（4/4基础） | Webhook、SARIF、Issue Sync、CI Status | CI Status 真实 API 回调 |
| 多模型治理 | 100%（6/6） | AI Gateway、策略绑定、Prompt模板、审计 | — |

---

## 一、Review Center 补齐计划

### 1.1 Review Timeline（审查时间线）

**目标**：展示一次 Review 的完整时间轴（提交 → 规则扫描 → AI审查 → Judge评估 → 人工确认 → 门禁结果）

**实施步骤**：
1. 新建后端 `infrastructure/timeline/ReviewTimelineService.java`
   - 从 Review + ReviewModelResult + PrePrGate 表中提取时间戳
   - 组装为 TimelineEvent 列表（eventType / timestamp / actor / description）
2. 新建 `domain/dto/ReviewTimelineVO.java` — 包含 List<TimelineEntry>
3. 在 `ReviewController` 中新增 `GET /api/reviews/{id}/timeline`
4. 前端 `reviews/detail.vue` 新增时间线组件：
   - 使用 `a-timeline` + `a-timeline-item` 展示
   - 左侧时间轴 + 右侧事件详情（步骤名、执行模型、耗时、状态）
   - 颜色编码：✅ 完成 / 🔵 运行中 / ❌ 失败

**文件**：`ReviewTimelineService.java`, `ReviewTimelineVO.java`, reviews/detail.vue（时间线区域）

### 1.2 Review Replay（审查回放）

**目标**：回放历史审查的完整分析过程

**实施步骤**：
1. 后端 `infrastructure/replay/ReviewReplayService.java`
   - 读取 ReviewModelResult.rawResult（SSE原始日志）
   - 反序列化为 Step 列表
2. 新增 `GET /api/reviews/{id}/replay`
3. 前端新增 `ReviewReplayPanel.vue` 组件：
   - 步骤式回放（上一步/下一步/自动播放）
   - 显示模型在每一步的 Thought → Action → Observation 循环

**文件**：`ReviewReplayService.java`, `ReviewReplayPanel.vue`, reviews/detail.vue（回放Tab）

### 1.3 Diff Context 前端展示

**目标**：在审查详情页直接展示 diff 原文，Reviewer可对照 Findings 查看代码

**实施步骤**：
1. 后端 `ReviewServiceImpl.createPrePrReview()` 中将 `diffContent` 存储到 Review 表（新增 `diff_content` 字段）
2. 审查详情 API 返回 `diffContent` 字段
3. 前端 `reviews/detail.vue` 新增"代码变更"Tab：
   - 渲染 DiffViewer 组件
   - Finding 卡片点击可滚动到对应行

**文件**：ReviewServiceImpl.java、数据库 migration、reviews/detail.vue

---

## 二、Pre-PR Gate 统一流水线引擎

### 目标

将现有的 7 个独立步骤（AI Rule Scan → Architecture Check → Security Scan → Multi-Agent Review → Judge → Human Confirmation → CI Gate Result）通过统一流水线引擎串联，支持可视化进度追踪。

### 实施步骤

1. 新建 `infrastructure/pipeline/PrePrPipelineEngine.java`
   - 定义 7 步流水线：`pipeline-steps.json`
   - 步骤间自动传递上下文（fileChanges、findings、gateDecision）
   - 每步执行后记录状态（PENDING → RUNNING → PASSED/BLOCKED）
   - 任何一步 BLOCKED 则终止后续步骤

2. 新建 `domain/dto/PrePrPipelineStatusVO.java`
   - pipelineId、reviewId、steps: [{stepKey, name, status, startedAt, completedAt, output}]

3. 新增 API：
   - `POST /api/reviews/{id}/pipeline/run` — 启动流水线
   - `GET /api/reviews/{id}/pipeline/status` — SSE 实时进度

4. 前端 `reviews/detail.vue` 新增流水线进度面板：
   - `a-steps` 组件展示 7 步当前状态
   - SSE 实时更新每步状态

**文件**：`PrePrPipelineEngine.java`, `PrePrPipelineStatusVO.java`, `pipeline-steps.json`, reviews/detail.vue

---

## 三、Rule Governance Center 补齐

### 3.1 DDO 规则类型

**目标**：补充 Domain-Driven Object 规则

**实施步骤**：
1. 在 `RuleRepository.java` 的 `internalRules()` 中新增：
   - `ddo-entity-no-setter`：实体不应有公开无参 setter
   - `ddo-value-object-immutable`：值对象字段应为 final
   - `ddo-aggregate-root-only`：仅 AggregateRoot 可引用 Repository

2. 对应前端 `governance.vue` 规则包中增加 DDO 分类展示

**文件**：`RuleRepository.java`

### 3.2 规则发现页面增强

**目标**：前端 governance.vue 增加规则库浏览和管理能力

**实施步骤**：
1. 调用 `GET /api/rules` 展示所有规则（带分类过滤、启用/禁用开关）
2. 每条规则展示：名称、分类、严重度、命中条件数、启用状态
3. 新增规则启用/禁用切换

**文件**：`governance.vue`

---

## 四、Agent Runtime 6 维补全

### 现状

| Agent | Role | Memory | Skill | Rule | Tool | Workflow |
|-------|------|--------|-------|------|------|----------|
| Review | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Judge | ✅ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Architecture | ✅ | ✅ | ⬜ | ✅ | ⬜ | ⬜ |
| Security | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| Performance | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| Refactor | ✅ | ✅ | ⬜ | ✅ | ⬜ | ⬜ |
| Test | ✅ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Release | ✅ | ⬜ | ⬜ | ⬜ | ⬜ | ✅ |

### 4.1 Skill 补齐（Architecture / Refactor / Test）

**实施步骤**：
1. 新建 `infrastructure/agent/skill/ArchitectureSkill.java`
   - 注入架构规则（分层、依赖方向、循环依赖检测）
2. 新建 `infrastructure/agent/skill/RefactorSkill.java`
   - 注入重构模式（提取方法、提取类、移动方法）
3. 新建 `infrastructure/agent/skill/TestGenerationSkill.java`
   - 注入测试生成规则（边界值、等价类、异常分支覆盖）

**文件**：3 个 Skill 实现类 + AgentRoleTemplates 注册

### 4.2 Tool 补齐（Architecture / Security / Performance）

**实施步骤**：
1. 新建 `infrastructure/agent/tool/DependencyGraphTool.java`
   - 解析 pom.xml/gradle 的依赖树，检测循环依赖
2. 新建 `infrastructure/agent/tool/SastTool.java`
   - 整合已有的规则引擎 Security 规则，提供独立安全扫描Tool
3. 新建 `infrastructure/agent/tool/ProfilerTool.java`
   - 检查代码中的潜在热点（循环内IO、大对象创建）

**文件**：3 个 Tool 实现类

### 4.3 Memory 补齐（Judge / Test / Release）

**实施步骤**：
1. 在 `TeamMemoryStore.java` 的 `seedMemories()` 中新增：
   - Judge 相关记忆（误报模式、质量评估标准）
   - Test 相关记忆（覆盖率要求、必须测试的核心路径）
   - Release 相关记忆（发布检查清单、回滚条件）

**文件**：`TeamMemoryStore.java`

---

## 五、Workflow Engine 补齐

### 目标

将 4 个缺失的工作流注册为正式工作流模板。

### 实施步骤

1. **Emergency Fix Workflow**
   - 新建 `infrastructure/workflow/EmergencyFixWorkflow.java`
   - 3 步：跳过常规门禁 → 最小化审查 → 紧急合并 → 事后补齐审查
2. **Architecture Review Workflow**
   - 注册到 `SimpleWorkflowEngine`，调用 `ArchitectureCommitteeOrchestrator`
3. **Security Audit Workflow**
   - 注册到 `SimpleWorkflowEngine`，调用 `SecuritySkill` + 规则引擎安全规则
4. **Refactor Workflow**
   - 注册到 `SimpleWorkflowEngine`，调用 `RefactorPlannerAgent`
5. 前端 `governance.vue` 工作流模板区域展示 6 个工作流（目前只有 4 个）

**文件**：`EmergencyFixWorkflow.java` + governance.vue 更新

---

## 六、CI/CD 集成完善

### CI Status Check 真实 API 回调

**目标**：`HttpCiStatusService` 从日志桩升级为真实 HTTP 回调

**实施步骤**：
1. 新建 `infrastructure/ci/GitHubStatusClient.java`
   - 调用 GitHub Commit Status API：`POST /repos/{owner}/{repo}/statuses/{sha}`
   - state: success/failure/pending, context: "AI Review Gate"
2. 新建 `infrastructure/ci/GitLabStatusClient.java`
   - 调用 GitLab Commit Status API
3. 在 `CiConnectionConfig` 中配置 token 和 endpoint
4. `HttpCiStatusService` 注入 `GitHubStatusClient` / `GitLabStatusClient`，根据项目配置路由

**文件**：`GitHubStatusClient.java`, `GitLabStatusClient.java`, `CiConnectionConfig.java`, `HttpCiStatusService.java`

---

## 七、前端页面更新计划

| 页面 | 更新内容 |
|------|----------|
| `reviews/detail.vue` | 新增：Timeline时间线 Tab、Replay回放 Tab、代码变更 Tab、流水线步骤条 |
| `governance.vue` | 规则库浏览管理、DDO规则类型、6个工作流模板 |
| `knowledge/index.vue` | —（已可用） |
| `gateway/index.vue` | —（已可用） |
| `models.vue` | —（已可用） |

---

## 八、执行顺序（依赖关系）

```
第一阶段（可并行）
├── 1.1 Review Timeline ─────── 独立
├── 1.2 Review Replay ───────── 独立
├── 3.1 DDO Rules ───────────── 独立
├── 4.3 Memory 补齐 ──────────── 独立
└── 5.x Workflow 补齐 ────────── 独立

第二阶段
├── 2.x Pre-PR Pipeline Engine ─ 依赖 ReviewServiceImpl
├── 4.1 Skill 补齐 ───────────── 独立
├── 4.2 Tool 补齐 ────────────── 独立
└── 6.x CI Status 真实回调 ───── 独立

第三阶段
├── 1.3 Diff Context 展示 ───── 需要 DB migration + 前端
├── 3.2 规则发现页面增强 ────── 依赖后端 GET /api/rules
└── 7.x 前端页面更新 ─────────── 汇总以上所有变更

验证
└── 后端编译 + 前端测试 + 前端构建
```

---

## 九、新增文件清单

### 后端（Java）

| 文件 | 行数估 | 用途 |
|------|--------|------|
| `ReviewTimelineService.java` | ~80 | 审查时间线服务 |
| `ReviewTimelineVO.java` | ~30 | 时间线 VO |
| `ReviewReplayService.java` | ~60 | 审查回放服务 |
| `PrePrPipelineEngine.java` | ~150 | Pre-PR 流水线引擎 |
| `PrePrPipelineStatusVO.java` | ~40 | 流水线状态 VO |
| `pipeline-steps.json` | ~40 | 流水线步骤定义 |
| `ArchitectureSkill.java` | ~50 | 架构审查 Skill |
| `RefactorSkill.java` | ~50 | 重构 Skill |
| `TestGenerationSkill.java` | ~50 | 测试生成 Skill |
| `DependencyGraphTool.java` | ~80 | 依赖图 Tool |
| `SastTool.java` | ~60 | 安全扫描 Tool |
| `ProfilerTool.java` | ~60 | 性能分析 Tool |
| `EmergencyFixWorkflow.java` | ~40 | 紧急修复工作流 |
| `GitHubStatusClient.java` | ~60 | GitHub Status API 客户端 |
| `GitLabStatusClient.java` | ~60 | GitLab Status API 客户端 |
| `CiConnectionConfig.java` | ~30 | CI 连接配置 |

### 前端（Vue）

| 文件 | 用途 |
|------|------|
| `components/review/ReviewTimeline.vue` | 时间线组件 |
| `components/review/ReviewReplayPanel.vue` | 回放面板组件 |
| `reviews/detail.vue`（增强） | 新增 Tab |
| `governance.vue`（增强） | 规则管理 + 工作流列表 |

### 数据库

| 变更 | 说明 |
|------|------|
| `review` 表新增字段 `diff_content TEXT` | 存储原始 diff 内容 |

---

## 十、质量保障

```
✅ 数据库迁移：只新增一个 nullable 字段，不修改现有表结构
✅ 前端纯函数：如有新增 util，先写测试再实现
✅ 每次变更后：npm test + npm run build（前端）+ mvn compile（后端）
✅ 最终验证：49 测试通过 + 后端 204 文件编译通过
```
