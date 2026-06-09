# Agent 不是 Prompt：Role、Skill、Tool、Memory 缺一不可

封面图中文提示词：

> 一张公众号科技文章封面图，主题是“Agent 不是 Prompt：Role、Skill、Tool、Memory 缺一不可”。画面中心是一个工程化 Agent 核心，周围环绕 Role、Skill、Tool、Memory、Rule、Workflow 六个模块，底部连接 Git、代码库、CI、Issue、知识库等工程系统。整体风格现代、工程化、克制，深色科技背景，蓝色代码流、绿色能力模块、橙色风险提示点缀，不要文字、不要 Logo、不要水印，适合宽幅公众号封面。

上一篇我们讲了 Judge Agent。

Judge 的价值，是把多个模型的输出整理成可消费的风险结论。

但讲到这里，其实还有一个更底层的问题：

> 我们一直在说 Agent，可 Agent 到底是什么？

很多团队刚开始做 Agent 时，很容易把 Agent 理解成一段更复杂的 Prompt。

比如：

> 你是一个资深 Java 架构师，请帮我审查下面这段代码，重点关注安全、性能、异常处理和代码规范。

这当然能工作。

甚至在 Demo 阶段，它看起来还挺聪明。

但一旦进入真实研发流程，问题就会出现。

今天想让它看安全，就在 Prompt 里加一句“重点关注安全”。

明天想让它看异常处理，就再加一段“请检查异常分支”。

后天想让它读项目规范，又把规范复制进 Prompt。

再往后，想让它读文件、查 Git、保存 Finding、进入 Pre-PR Gate，就继续往 Prompt 里塞说明。

最后，这段 Prompt 会越来越长，越来越脆弱，也越来越难维护。

这就是为什么我一直觉得：

> Agent 不是 Prompt。Prompt 只是 Agent 的一小部分。

一个工程 Agent，至少需要 Role、Skill、Tool、Memory、Rule、Workflow。

缺任何一块，它都很容易退回到“会说话的模型”。

## 只写 Prompt 为什么很快会失控？

Prompt 最大的优点是灵活。

但它最大的风险，也是灵活。

因为所有东西都写进 Prompt 之后，边界会变得很模糊。

角色写在里面。

规范写在里面。

工具使用方式写在里面。

输出格式写在里面。

业务背景也写在里面。

一旦模型输出不稳定，我们很难判断到底是哪一层出了问题。

是角色定义不清？

是技能描述太泛？

是上下文给错了？

是工具结果污染了？

是团队规则本身没有共识？

还是模型能力不适合这个任务？

如果所有能力都混在一段 Prompt 里，调试 Agent 就会变成调玄学。

配图提示词：

> 一张对比图，左侧是“Prompt-only Agent”，所有角色、规范、工具说明、输出格式都挤在一大段混乱文本里；右侧是“工程化 Agent”，Role、Skill、Tool、Memory、Rule、Workflow 分层清晰，通过 Agent Runtime 编排。整体风格清晰、工程化、适合技术公众号正文配图，浅色背景，蓝绿主色，不要 Logo，不要水印。

所以，工程化的第一步，是把 Prompt 拆开。

不是拆成更多 Prompt。

而是拆成更清晰的结构。

## Agent 的最小工程结构

在 Review Agent 这样的场景里，一个 Agent 至少应该包含六个部分。

第一，Role。

它决定 Agent 是谁，从什么视角看问题。

第二，Skill。

它决定 Agent 具备哪些专业能力，按什么方法做判断。

第三，Tool。

它决定 Agent 能不能进入工程现场，读取真实代码、Git 历史、CI 状态和审查结果。

第四，Memory。

它决定 Agent 能不能复用团队过去的经验，而不是每次从零开始。

第五，Rule。

它决定 Agent 必须遵守哪些团队硬约束。

第六，Workflow。

它决定 Agent 在什么时候被触发，产物进入哪个流程，失败后谁处理。

可以把它抽象成这样：

```yaml
engineeringAgent:
  role:
    name: ARCHITECT_REVIEWER
    responsibility: "识别架构边界、分层依赖和长期演进风险"
  skills:
    - JavaCodeStyleSkill
    - PerformanceSkill
  tools:
    - GitDiffTool
    - ReadFileTool
    - CodeStructureTool
    - SaveFindingTool
  memory:
    - TeamArchitectureDecisions
    - HistoricalFindings
    - DismissedRiskPatterns
  rules:
    - "Controller 不允许直接访问数据库"
    - "Application 层不允许依赖 Infrastructure 实现"
  workflow:
    trigger: "Pre-PR Gate"
    output: "Structured Finding"
    nextStep: "Gate Policy / Human Review"
```

这时候 Agent 才不再是一段“会说话的文本”。

它变成了一个可配置、可执行、可观测、可复盘的工程单元。

配图提示词：

> 一张 Agent 最小工程结构图，中心是 Engineering Agent，周围六个模块：Role、Skill、Tool、Memory、Rule、Workflow，每个模块都有简短的工程含义，底部输出 Structured Finding 和 Gate Decision。风格现代、克制、信息清晰，适合公众号正文配图，不要 Logo，不要水印。

## Role：先让 Agent 知道自己是谁

Role 不是一句“你是专家”。

Role 是职责边界。

在 Code Review 场景里，不同 Agent 应该有不同关注点。

比如：

- `SECURITY_AUDITOR`：关注鉴权、敏感信息、注入风险、越权访问。
- `PERFORMANCE_ANALYST`：关注慢查询、循环调用、缓存策略、资源释放。
- `CODE_STYLE_CHECKER`：关注命名、分层、结构、可读性。
- `EXCEPTION_HANDLER`：关注异常分支、错误码、重试、降级。
- `ARCHITECT_REVIEWER`：关注模块边界、依赖方向、领域模型和长期演进。

这些角色不应该混成一个“全能 Reviewer”。

因为全能听起来强，实际很容易泛。

一个安全审计 Agent，不应该花大量篇幅讨论变量命名。

一个代码规范 Agent，也不应该强行裁决业务架构取舍。

Role 的价值，就是收窄注意力。

它告诉 Agent：

> 你不用什么都看，但你负责的部分要看深。

项目里的前端类型已经把 Agent 角色显式枚举出来：

```ts
export type AgentRole =
  | 'SECURITY_AUDITOR'
  | 'PERFORMANCE_ANALYST'
  | 'CODE_STYLE_CHECKER'
  | 'EXCEPTION_HANDLER'
  | 'ARCHITECT_REVIEWER'
```

这看起来只是一个类型定义。

但它背后其实是一个很重要的工程选择：

> Agent 的职责要被结构化，而不是藏在 Prompt 里。

只有职责结构化，后面才有可能做策略编排、结果归因和效果评估。

## Skill：把专业经验变成可复用能力

Role 解决“你是谁”。

Skill 解决“你会什么”。

一个 `SECURITY_AUDITOR` 不能只靠一句“请关注安全问题”来工作。

它需要知道安全审查的方法：

- 是否存在明文密钥。
- 是否绕过统一鉴权。
- 是否有 SQL 注入风险。
- 是否暴露敏感字段。
- 是否缺少权限边界。
- 是否把内部异常直接返回给外部调用方。

这些方法不应该每次都临时写进 Prompt。

它们应该被封装成 Skill。

比如项目里已经出现了几类典型 Skill：

```yaml
skills:
  JavaCodeStyleSkill:
    focus:
      - "命名和结构"
      - "分层约束"
      - "代码可读性"
  SecuritySkill:
    focus:
      - "明文密钥"
      - "鉴权绕过"
      - "敏感信息泄露"
  PerformanceSkill:
    focus:
      - "循环查库"
      - "慢查询风险"
      - "缓存和资源释放"
  ExceptionHandlingSkill:
    focus:
      - "异常分支"
      - "错误码语义"
      - "重试和降级"
```

Skill 的价值，是让经验可复用。

今天它可以被 `quality-gate` 策略使用。

明天它可以被 `architecture-board` 策略使用。

后天它还可以进入测试生成、发布检查、重构规划。

如果经验只写在 Prompt 里，它很难复用。

如果经验变成 Skill，它就可以被编排。

这就是 Agent 工程化里非常关键的一步：

> 把专家经验从自然语言提示，升级为可被系统引用的能力模块。

## Tool：没有工具的 Agent 只能评论

Role 和 Skill 解决的是认知问题。

Tool 解决的是行动问题。

一个没有工具的 Agent，只能基于你给它的上下文说话。

它不能自己读文件。

不能自己查 Git 历史。

不能自己定位调用链。

不能自己保存 Finding。

不能把结果同步到 Issue 或 Review 页面。

这类 Agent 更像“评论员”。

真正进入工程现场的 Agent，必须有 Tool。

比如 Code Review 场景里，至少需要这些工具：

```yaml
tools:
  GitDiffTool:
    purpose: "获取源分支和目标分支之间的代码变更"
  ReadFileTool:
    purpose: "读取变更文件周边上下文"
  CodeStructureTool:
    purpose: "分析类、方法、依赖和调用关系"
  GitLogTool:
    purpose: "查看相关文件历史变更"
  SaveFindingTool:
    purpose: "把审查发现保存为结构化 Finding"
```

注意最后一个 `SaveFindingTool`。

它很重要。

因为 Agent 的价值不只是“看完代码说几句建议”。

它要把结果写回工程系统。

否则每一次审查都只是一次聊天记录。

有了 Tool，Agent 才能从“生成内容”变成“参与流程”。

配图提示词：

> 一张 Agent Tool Layer 图，左侧是 Agent Runtime，中间连接 GitDiffTool、ReadFileTool、CodeStructureTool、GitLogTool、SaveFindingTool，右侧连接代码仓库、Git 历史、代码结构、Review Finding 数据库。风格工程化、清晰、适合公众号正文配图，不要 Logo，不要水印。

## Memory：让团队经验不要每次归零

如果 Agent 每次 Review 都从零开始，它就很难成为团队资产。

它可能知道通用最佳实践。

但它不知道这个团队过去发生过什么。

它不知道哪些问题曾经导致线上事故。

它不知道哪些建议经常被团队驳回。

它不知道哪些架构边界是历史原因形成的。

它也不知道哪些规范已经在团队里达成共识。

这就是 Memory 的价值。

Memory 不是让模型“记住聊天记录”。

在工程治理里，Memory 更应该是结构化的团队经验库。

比如：

```yaml
teamMemory:
  historicalFindings:
    - "过去 30 天内重复出现的高频问题"
    - "曾经导致线上故障的风险模式"
  humanDecisions:
    - "哪些 AI Finding 被确认"
    - "哪些 AI Finding 被驳回"
    - "哪些风险被接受并设置过期时间"
  architectureDecisions:
    - "为什么当前模块边界这样划分"
    - "哪些依赖方向不允许突破"
  ruleLearningCandidates:
    - "高频 Finding 可沉淀为 Rule"
    - "高频人工驳回可反向优化 Skill"
```

这样 Agent 下次审查时，就不只是拿着通用知识看代码。

它还会带着团队自己的历史经验。

这一步非常关键。

因为 AI Coding 治理的长期价值，不是每次把问题找出来。

而是让系统越来越懂这个团队。

> 没有 Memory，Agent 只是一次性工具。  
> 有了 Memory，Agent 才可能变成持续进化的治理系统。

## Rule：把硬约束从建议变成默认执行

Role、Skill、Tool、Memory 还不够。

Agent 还需要 Rule。

Skill 更像专业能力。

Rule 更像硬约束。

比如：

- 禁止 Controller 直接访问数据库。
- 禁止明文 AK/SK。
- Application 层不允许依赖 Infrastructure 实现。
- 核心链路库存扣减必须使用原子条件更新。
- 对外接口不能直接暴露内部异常堆栈。

这些规则不应该每次靠人提醒。

也不应该只是写在团队规范文档里。

它们应该变成 Agent 执行时的默认约束。

一个 Rule 可以长这样：

```yaml
rule:
  id: architecture-controller-no-db
  name: "Controller 不允许直接访问数据库"
  severity: BLOCKER
  scope:
    include:
      - "src/main/java/**/controller/**/*.java"
  condition:
    forbiddenDependencies:
      - "Mapper"
      - "Repository"
      - "JdbcTemplate"
  action:
    gate: BLOCK
    findingCategory: CODE_STYLE
```

Rule 的价值，是让团队共识可执行。

它不是替代 Skill。

它是给 Skill 和 Agent Runtime 提供不可突破的边界。

如果 Skill 是“怎么判断”，Rule 就是“哪些事情绝对不能发生”。

## Workflow：Agent 必须进入真实流程

最后一块是 Workflow。

没有 Workflow，Agent 仍然只是一个可以手动调用的工具。

有 Workflow，Agent 才知道：

- 什么时候触发。
- 输入从哪里来。
- 输出写到哪里。
- 失败如何处理。
- 谁有权确认。
- 下一步进入哪个系统。

比如同样是代码审查 Agent，它可以进入不同 Workflow：

```yaml
workflows:
  prePrGate:
    trigger: "开发提交 PR 前"
    input: "sourceBranch vs targetBranch diff"
    output: "Gate Result + Findings + PR Summary"
    next:
      - "PASSED -> 创建正式 PR"
      - "BLOCKED -> 返回开发修复"
      - "NEEDS_HUMAN_REVIEW -> 技术负责人确认"
  formalReview:
    trigger: "正式 PR 创建后"
    input: "PR diff + Pre-PR findings"
    output: "Review Timeline + Reviewer 辅助摘要"
  releaseCheck:
    trigger: "发布前"
    input: "变更列表 + 风险记录 + 测试状态"
    output: "发布风险报告"
```

Workflow 的价值，是把 Agent 从“单点能力”变成“流程节点”。

这也是为什么前几篇一直强调：我们需要的不是 AI Review 小工具，而是 AI 工程治理平台。

工具只解决一次调用。

Workflow 才能解决持续运行。

## AgentConfig 应该长什么样？

当 Role、Skill、Tool、Memory、Rule、Workflow 都拆开之后，Agent 的配置就可以变得很清晰。

项目里的前端类型已经有类似结构：

```ts
export interface AgentConfig {
  role: AgentRole
  modelName: string
  systemPrompt?: string
  skills: string[]
  temperature?: number
  maxSteps?: number
}

export interface AgentPipelineConfig {
  agents: AgentConfig[]
  orchestrationStrategy: 'PARALLEL' | 'SEQUENTIAL' | 'HYBRID'
  mcpEnabled: boolean
  mcpServerUrls: string[]
}
```

如果继续扩展到完整工程治理场景，可以变成：

```yaml
agentConfig:
  role: SECURITY_AUDITOR
  modelName: qwen-max
  skills:
    - SecuritySkill
  tools:
    - GitDiffTool
    - ReadFileTool
    - SaveFindingTool
  memory:
    collections:
      - historicalSecurityFindings
      - acceptedRiskRecords
  rules:
    packs:
      - security-baseline
      - secret-leak-prevention
  output:
    format: StructuredFinding
    requireEvidence: true
    requireConfidence: true
  workflow:
    trigger: PrePrGate
    gatePolicy:
      blockOn:
        - BLOCKER
      requireHumanReviewOn:
        - MAJOR
```

这里的 Prompt 仍然存在。

但它不再承载全部复杂度。

Prompt 只是让模型理解当前任务的表达层。

真正的工程能力，被拆到了配置、Skill、Tool、Memory、Rule 和 Workflow 里。

这才是可维护的 Agent。

## 工程 Agent 的判断标准

怎么判断一个 Agent 是否已经工程化？

我觉得可以问六个问题。

第一，它有没有明确 Role？

如果一个 Agent 什么都负责，最后往往什么都看不深。

第二，它有没有可复用 Skill？

如果所有专业经验都藏在 Prompt 里，就很难复用和升级。

第三，它有没有 Tool？

如果它不能读代码、查上下文、保存结果，那它只能停留在评论层。

第四，它有没有 Memory？

如果它不能吸收团队历史经验，就会每次从通用最佳实践重新开始。

第五，它有没有 Rule？

如果没有硬约束，Agent 的建议就很容易被模型风格左右。

第六，它有没有 Workflow？

如果它不能进入 Pre-PR、Review、发布、测试这些真实流程，它就很难成为研发基础设施。

这六个问题，基本可以区分“一个会聊天的模型”和“一个可落地的工程 Agent”。

配图提示词：

> 一张工程 Agent 成熟度检查表图，六个检查项依次为 Role、Skill、Tool、Memory、Rule、Workflow，每个检查项右侧有“缺失时的问题”和“具备后的价值”。整体风格像技术治理看板，清晰、克制、适合公众号正文配图，不要 Logo，不要水印。

## 结尾

Agent 不是 Prompt。

Prompt 很重要，但它只是入口。

真正能进入研发流程的 Agent，必须有清晰的 Role、可复用的 Skill、可调用的 Tool、可沉淀的 Memory、可执行的 Rule，以及可编排的 Workflow。

这也是 AI Coding 治理从个人工具走向团队系统的关键一步。

个人工具可以靠 Prompt。

团队系统必须靠结构。

当 Agent 被拆成这些工程组件之后，我们才能继续讨论下一层问题：

> Agent 如何真正进入工程现场？

这就会引出下一篇：

> MCP 和工具层：Agent 如何读代码、查 Git、跑工具，而不是只聊天。
