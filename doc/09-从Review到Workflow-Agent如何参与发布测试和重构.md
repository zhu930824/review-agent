# 从 Review 到 Workflow：Agent 如何参与发布、测试和重构？

封面图中文提示词：

> 一张公众号科技文章封面图，主题是“从 Review 到 Workflow：Agent 如何参与发布、测试和重构？”。画面中心是一个 Agent Workflow Engine，从左侧的代码变更和 Review Finding 出发，连接到测试生成、发布门禁、重构规划、风险评估、技术债治理等多个流程节点。画面要体现“从单点审查到研发流程执行者”的演进。整体风格现代、工程化、克制，深色科技背景，蓝色流程线、绿色通过节点、橙色风险节点点缀，不要文字、不要 Logo、不要水印，适合宽幅公众号封面。

上一篇我们讲了 Rule / Skill / SOP。

它们解决的是一个核心问题：

> 如何把团队经验变成 AI 可以执行的约束。

但一套约束真正跑起来之后，Agent 的价值就不应该只停在 Review。

Review 很重要。

它是入口。

但它不是终点。

因为在真实研发流程里，代码变更从来不是“被审查完”就结束了。

它还要测试。

要发布。

要评估风险。

要处理技术债。

要生成修复草案。

要同步 Issue。

要沉淀规则。

如果 Agent 只在 Review 阶段出现，它看到的是一个点。

如果 Agent 进入 Workflow，它看到的才是一条链路：从代码变更、风险发现，到测试补齐、发布准入、重构拆解和技术债沉淀。

这就是第九篇想讲的主题：

> Agent 平台的下一步，不是把 Review 做得更花，而是从 Review 走向 Workflow。

## Review 只是入口

为什么 Review 会成为 Agent 平台的第一个入口？

因为它天然适合 AI。

输入明确：代码 diff。

目标明确：发现问题。

输出明确：Finding、摘要、风险等级、修复建议。

价值也明确：减少人工 Reviewer 的基础负担。

所以大多数 AI 工程化平台，都会从 Review 起步。

但 Review 有一个天然边界：

> 它只能告诉你“这里可能有问题”，不能天然保证“这个变更可以安全交付”。

一个 PR 没有明显代码问题，不代表测试覆盖足够。

测试通过了，也不代表发布风险可控。

发布风险可控，也不代表技术债没有继续累积。

技术债被发现了，也不代表它会进入后续迭代。

所以，如果 Agent 只停留在 Review，它最终会变成一个更聪明的检查器。

检查器能发现问题。

治理系统要推动问题进入下一步。

配图提示词：

> 一张“Review 只是入口”的流程图，左侧是 Code Review，后面依次连接 Test Generation、Release Gate、Risk Assessment、Refactor Plan、Tech Debt Tracking，表达代码审查只是研发治理链路第一站。风格清晰、工程化、适合公众号正文配图，不要 Logo，不要水印。

## 发布工作流：从 Finding 到 Gate

先看发布。

发布是最容易体现 Agent Workflow 价值的场景。

因为发布不是一个单点动作，而是一组状态判断，也是一组责任确认。

比如：

- 代码审查是否完成？
- 是否存在 BLOCKER？
- MAJOR 是否有人确认？
- CI 是否通过？
- 测试证据是否齐全？
- 是否需要安全负责人审批？
- 是否有未关闭的高风险 Finding？
- 是否允许例外放行？

这些问题靠人逐个检查，很容易漏。

靠一段 Prompt，也不够稳定。

更合理的方式，是把发布变成 Workflow。

项目里已经有类似的发布步骤结构：

```ts
export interface WorkflowStep {
  stepKey: string
  name: string
  type: string
  responsible: string
  order: number
  dependsOn: string
  status: string
}

export interface ReleaseWorkflow {
  reviewId: number
  workflowId: string
  steps: WorkflowStep[]
  currentStep: string
  status: string
}
```

这意味着发布不再只是“点一下上线”。

它可以被拆成一组可观测步骤。

比如：

```yaml
releaseWorkflow:
  reviewId: 1024
  workflowId: release-gate
  steps:
    - stepKey: ai_review
      name: "AI Review 完成"
      type: CHECK
      responsible: Agent
      status: COMPLETED
    - stepKey: blocker_gate
      name: "BLOCKER 阻断检查"
      type: GATE
      responsible: GatePolicy
      dependsOn: ai_review
      status: COMPLETED
    - stepKey: major_confirm
      name: "MAJOR 人工确认"
      type: HUMAN
      responsible: TechLead
      dependsOn: blocker_gate
      status: REQUIRED
    - stepKey: ci_check
      name: "CI 状态检查"
      type: GATE
      responsible: CI
      dependsOn: major_confirm
      status: PENDING
    - stepKey: release_approval
      name: "发布审批"
      type: HUMAN
      responsible: ReleaseManager
      dependsOn: ci_check
      status: PENDING
```

这里的关键不是 YAML。

关键是发布状态变成了可解释的流程状态。

Agent 可以告诉你：

> 当前不能发布，不是因为“系统不允许”，而是因为 MAJOR 风险还没有技术负责人确认，CI 也还没有完成。

这就比一句“发布失败”有价值得多。

因为它告诉团队：卡住发布的不是情绪，不是模糊判断，而是一个可定位、可处理、可追踪的流程节点。

配图提示词：

> 一张 Release Workflow 状态图，从 AI Review、BLOCKER Gate、MAJOR Human Confirm、CI Check、Release Approval 到 Ready to Release，每个节点有 COMPLETED / REQUIRED / PENDING / BLOCKED 状态，突出 Agent 如何把 Finding 转成发布门禁。风格工程化、清晰、适合公众号正文配图，不要 Logo，不要水印。

## 测试生成：人定范围，AI 补步骤

测试生成是第二个很适合 Agent 参与的场景。

但这里有一个坑：

> 不要让 AI 完全定义测试范围。

AI 可以扫描代码。

可以补充边界条件。

可以生成测试步骤。

可以根据 diff 推断影响面。

但它不一定知道业务真实风险。

比如一个看起来很小的字段改动，可能影响核心计费。

一个代码层面很复杂的改动，可能只是内部管理后台的小优化。

所以测试生成最合理的方式，不是“AI 全自动生成所有用例”。

而是：

> 人定义范围和风险，AI 补充用例和步骤。

项目里的测试用例结构已经体现了这个思路：

```ts
export interface TestCase {
  testName: string
  targetMethod: string
  testLevel: string
  riskLevel: string
  inputs: string[]
  expectedOutput: string
  preconditions: string
  testCategory: string
}

export interface TestCoveragePlan {
  reviewId: string
  totalInterfaces: number
  highRiskCount: number
  mediumRiskCount: number
  lowRiskCount: number
  testCases: TestCase[]
  uncoveredPaths: string[]
  coverageSummary: string
}
```

这不是简单的“生成几条测试用例”。

它要求 Agent 输出一个测试覆盖计划。

这个计划里至少要包含：

- 涉及多少接口。
- 哪些是高风险路径。
- 哪些是中低风险路径。
- 每条用例属于单测、集成测试还是 E2E。
- 哪些路径还没有覆盖。
- 整体覆盖情况如何。

一个测试生成 Workflow 可以这样设计：

```yaml
testGenerationWorkflow:
  input:
    - diffContext
    - reviewFindings
    - humanRiskScope
  humanDefines:
    - "本次必须覆盖的核心业务路径"
    - "哪些接口属于高风险"
    - "哪些行为必须保持兼容"
  aiGenerates:
    - "测试用例列表"
    - "边界条件"
    - "异常分支"
    - "未覆盖路径"
    - "测试数据建议"
  output:
    - TestCoveragePlan
    - HighRiskTestCases
    - UncoveredPaths
```

这里最重要的是 `humanRiskScope`。

人先告诉 Agent 什么重要。

Agent 再做扫描和补全。

这个顺序不能反。

否则 AI 很容易把大量边缘用例铺满页面，却漏掉真正会影响业务的核心路径。

配图提示词：

> 一张测试生成协作图，左侧是人定义测试范围、核心路径和风险等级，中间是 Agent 扫描 diff、Finding 和代码结构，右侧输出 TestCoveragePlan、High Risk Cases、Uncovered Paths。突出“人定范围，AI 补步骤”。风格清晰、工程化、适合公众号正文配图，不要 Logo，不要水印。

## 重构规划：AI 负责拆解，人负责取舍

第三个场景是重构。

重构最怕两件事。

第一，太大。

一说重构，就变成一个巨大的专项，最后很难排期。

第二，太散。

每次业务需求顺手改一点，但没有计划，最后结构还是继续腐化。

Agent 在重构里的价值，不是替人决定“要不要重构”。

而是把重构拆小。

比如：

- 哪些文件最值得优先处理？
- 哪些步骤是安全的？
- 哪些步骤会有破坏性？
- 哪些迁移可以夹在业务需求里做？
- 哪些必须单独排期？
- 哪些风险需要人工确认？

项目里的重构计划结构就很适合表达这个过程：

```ts
export interface RefactorStep {
  stepName: string
  targetFile: string
  description: string
  approach: string
  priority: string
  breaking: boolean
  estimatedEffort: string
}

export interface RefactorPlan {
  planId: string
  title: string
  summary: string
  totalSteps: number
  estimatedImpact: number
  steps: RefactorStep[]
  prerequisites: string[]
  risks: string[]
}
```

这几个字段很关键。

`priority` 让团队知道先做什么。

`breaking` 让团队知道哪些步骤有破坏性。

`estimatedEffort` 帮助判断能不能塞进当前迭代。

`prerequisites` 告诉团队前置条件是什么。

`risks` 告诉团队哪些地方要人工确认。

一个重构规划 Workflow 可以这样写：

```yaml
refactorPlanningWorkflow:
  trigger: "Review 中发现重复架构问题或高频技术债"
  inputs:
    - historicalFindings
    - codeStructure
    - changeRisk
    - teamRules
  aiOutputs:
    - RefactorPlan
    - SafeSteps
    - BreakingSteps
    - EstimatedEffort
  humanDecisions:
    - "是否接受重构方向"
    - "哪些步骤进入当前迭代"
    - "哪些步骤转成技术债"
    - "哪些破坏性步骤需要架构评审"
```

这就是 AI 和人的合理分工。

AI 擅长扫描、归类、拆解、估算。

人负责判断优先级、业务节奏和风险接受度。

> 重构不是让 AI 一口气改完，而是让 AI 把复杂工作拆成团队能消化的步骤。

配图提示词：

> 一张重构规划流程图，从 Historical Findings、Code Structure、Team Rules 输入到 Agent Refactor Planner，输出 Safe Steps、Breaking Steps、Effort Estimate、Risk List，最后由 Tech Lead 选择进入当前迭代或转技术债。风格工程化、清晰、适合公众号正文配图，不要 Logo，不要水印。

## 风险预测：把感觉变成可解释信号

第四个场景是风险预测。

很多团队判断发布风险，主要靠经验。

这个需求改了核心链路，感觉风险高。

这个 PR 改了很多文件，感觉风险高。

这个同学不熟系统，感觉风险高。

这个模块历史问题多，感觉风险高。

这些判断不是没有价值。

但如果一直停留在“感觉”，就很难沉淀，也很难复盘。

Agent 可以做的一件事，是把这些感觉拆成可解释信号。

比如：

- 改动文件数量。
- 核心模块是否被修改。
- 是否涉及数据库结构。
- 是否涉及权限、安全、资金、库存。
- 是否有 BLOCKER 或 MAJOR Finding。
- 是否存在未覆盖路径。
- 是否有破坏性重构步骤。
- 相关文件历史故障是否较多。

项目里的风险评估结构很简单，但方向是对的：

```ts
export interface RiskAssessment {
  level: string
  score: number
  factors: string[]
  fileLevelRisks: string[]
  recommendation: string
}
```

这意味着风险不只是一个等级。

它还要有分数、因素、文件级风险和建议。

一个风险评估输出可以长这样：

```json
{
  "level": "HIGH",
  "score": 82,
  "factors": [
    "改动涉及核心优惠券核销链路",
    "存在 1 个 BLOCKER 和 2 个 MAJOR Finding",
    "库存扣减路径缺少并发测试",
    "本次变更包含 3 个破坏性重构步骤"
  ],
  "fileLevelRisks": [
    "CouponApplicationService.java: 核心业务编排和库存扣减",
    "CouponRepository.java: 数据一致性风险",
    "CouponController.java: 对外接口兼容性风险"
  ],
  "recommendation": "发布前必须完成并发扣减测试，并由 Tech Lead 确认 MAJOR Finding。"
}
```

这类输出的价值，在于让风险可讨论。

不是一句“我觉得风险高”。

而是：

> 系统认为风险高，因为它命中了这些因素。

人可以同意。

也可以驳回。

但驳回也会留下理由，反过来校准后续模型和规则。

配图提示词：

> 一张风险预测仪表图，左侧输入变更规模、核心模块、Finding、测试覆盖、重构步骤、历史故障，中间是 Change Risk Predictor，右侧输出 RiskAssessment：level、score、factors、fileLevelRisks、recommendation。风格现代、工程化、清晰，适合公众号正文配图，不要 Logo，不要水印。

## Agent Workflow 的通用结构

发布、测试、重构、风险预测，看起来是四个不同场景。

但它们背后有一套共同结构。

第一，触发条件。

Agent 不是随便运行，而是在某个流程节点被触发。

第二，工程上下文。

它需要 diff、Finding、规则包、历史记录、CI 状态、代码结构。

第三，Agent 执行。

不同 Agent 根据 Role、Skill、Tool、Memory 生成结构化结果。

第四，Gate 或人工确认。

高风险结论不能直接自动通过，必须进入 Gate Policy 或 Human Review。

第五，结果沉淀。

产物要保存成 TestCoveragePlan、ReleaseWorkflow、RefactorPlan、RiskAssessment，而不是停留在自然语言。

可以抽象成：

```yaml
agentWorkflow:
  trigger:
    type: "Pre-PR / PR / Release / Scheduled"
  context:
    - DiffContext
    - ReviewFindings
    - TeamRules
    - HistoricalMemory
    - CIStatus
  execution:
    - Role
    - Skill
    - Tool
    - ModelStrategy
  decision:
    - GatePolicy
    - HumanReview
    - AcceptRisk
  output:
    - StructuredArtifact
    - AuditLog
    - LearningSignal
```

这套结构一旦建立，Agent 平台就不再只是 Review 平台。

它会变成研发流程平台。

这时，Agent 的职责也发生了变化：它不再只是“给建议”，而是在流程里生成证据、推动决策、记录结果。

## 从单点工具到流程执行者

回头看整个系列，前几篇一直在讲一个转变：

AI Review 工具关注的是：

> 模型能不能发现代码问题。

Agent Workflow 平台关注的是：

> 发现问题之后，系统如何推动它进入正确流程。

这两者差别很大。

前者是点状能力。

后者是流程能力。

点状能力可以提升个人效率。

流程能力才能改变团队协作方式。

比如一个安全 Finding：

在工具里，它可能只是一条评论。

在 Workflow 里，它会触发：

- Gate 阻断。
- 安全负责人确认。
- 测试用例补充。
- 发布审批延后。
- SARIF 归档。
- 后续规则学习。

这才叫治理。

Agent 真正进入 Workflow 之后，它不再只是“帮你看代码”。

它开始参与团队如何交付软件。

## 结尾

Review 是 Agent 进入研发流程的入口。

但 Agent 的价值不应该停在 Review。

发布需要它把 Finding 转成 Gate 状态。

测试需要它补充用例、边界条件和未覆盖路径。

重构需要它扫描技术债、拆解步骤、标出破坏性风险。

风险评估需要它把模糊感觉变成可解释信号。

当这些能力被 Workflow 串起来，Agent 平台才真正从“审查工具”走向“研发治理系统”。

它不只是告诉你哪里有问题。

它还会推动问题进入流程。

推动流程留下证据。

推动证据反过来改进规则、Skill 和策略。

到这里，Agent 已经不只是研发流程里的助手。

它开始接近一种新的平台能力：把代码、规则、工具、流程和团队经验串起来。

这就来到了整个系列的最后一篇：

> 从 AI Review 工具到 AI Engineering Governance Platform。
