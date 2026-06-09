# Rule / Skill / SOP：把团队经验变成 AI 可执行约束

封面图中文提示词：

> 一张公众号科技文章封面图，主题是“Rule / Skill / SOP：把团队经验变成 AI 可执行约束”。画面中心是一套工程治理规则引擎，左侧是团队经验、架构规范、历史 Review、技术方案和 SOP 文档，右侧转化为 AI 可执行的 Rule、Skill、Workflow 和 Gate Policy。画面要体现“人人对齐、人机对齐、规则化、可执行约束”。整体风格现代、工程化、克制，深色科技背景，蓝色结构线、绿色规则模块、橙色风险门禁点缀，不要文字、不要 Logo、不要水印，适合宽幅公众号封面。

上一篇我们讲了 MCP 和工具层。

工具让 Agent 真正进入工程现场。

它可以读代码。

可以查 Git。

可以看 CI。

可以保存 Finding。

可以把 Gate 状态写回交付链路。

但工具只是“进入现场”的方式。

进入现场之后，Agent 还需要知道：

> 什么能做，什么不能做，什么必须阻断，什么可以例外。

这就进入了 Agent 工程化里最关键、也最容易被低估的一层：

> Rule / Skill / SOP。

很多团队在 AI Coding 之后，都会做一件事：

写规范。

比如工程分层规范、异常处理规范、数据库访问规范、缓存使用规范、安全基线、测试要求、发布检查清单。

这些规范当然重要。

但仅仅写成文档，远远不够。

因为文档最大的问题是：

> 它不会自动执行。

人可能不看。

新人可能理解错。

AI 可能不知道。

Reviewer 可能每次都要重复提醒。

最后，规范又回到了“靠人盯”的老路。

AI Coding 时代，真正重要的不是“有没有规范文档”。

而是：

> 能不能把团队经验变成 AI 可以执行的约束。

## 文档规范为什么经常失效？

传统研发团队并不缺规范。

很多团队都有架构规范。

有代码规范。

有发布规范。

有安全规范。

有测试规范。

甚至还有很长的 Wiki 页面。

但一到真实开发，规范还是会失效。

原因通常不是大家不认同规范。

而是规范没有进入执行链路。

开发写代码时，它不一定出现。

AI 生成代码时，它不一定被加载。

Pre-PR 自查时，它不一定被检查。

Code Review 时，它只能靠 Reviewer 记得。

发布前，它又变成一张人工 checklist。

所以规范经常处于一种很尴尬的状态：

> 它存在，但不在场。

AI Coding 之后，这个问题会被放大。

因为 AI 会快速生成大量代码。

如果代码库本身风格不统一，团队对规范理解不一致，AI 不会自动纠偏。

它很可能会学习现有混乱模式，把差异继续放大。

这也是为什么前面几篇一直强调：AI Coding 提升的不是“稳定产出好代码”的能力，而是“快速产出代码”的能力。

好不好，取决于约束。

配图提示词：

> 一张“规范存在但不在场”的流程图，左侧是 Wiki 文档、架构规范、发布 checklist，右侧是 AI Coding、Pre-PR、Code Review、Release 流程，中间有断开的连接，表示规范没有进入执行链路。下方对比“可执行约束”进入流程后的闭环。风格清晰、工程化、适合公众号正文配图，不要 Logo，不要水印。

## 先人人对齐，再人机对齐

把团队经验变成 AI 约束之前，有一个顺序不能反：

> 先人人对齐，再人机对齐。

很多团队容易跳过第一步。

他们看到 AI Rule、AI Prompt、Agent Skill，就马上开始写：

> 禁止 Controller 直接访问数据库。  
> Application 层不允许依赖 Infrastructure。  
> 异常必须统一处理。  
> 核心链路必须补充测试。

这些规则看起来都对。

但问题是：团队自己是否真的对齐了？

什么叫 Controller 直接访问数据库？

轻量查询算不算？

Application 层能不能依赖某个防腐接口？

异常统一处理的边界在哪里？

核心链路怎么定义？

哪些变更必须补测试，哪些可以例外？

如果这些问题没有先在人和人之间达成共识，那么写给 AI 的 Rule 也会变成一组模糊约束。

AI 执行时会摇摆。

Reviewer 解释时会摇摆。

开发绕过时也有理由。

最后，大家会觉得“AI Rule 不准”。

但真正的问题可能不是 AI。

而是团队标准没有对齐。

这也是评测 Agent 里很常见的逻辑：

先让人类标注标准一致。

再让模型去学习和执行这个标准。

如果人类自己都不一致，模型一致率没有意义。

工程治理也是一样。



```yaml
alignmentOrder:
  step1_humanAlignment:
    goal: "团队先统一判断标准"
    examples:
      - "什么是架构越界"
      - "什么是必须阻断的安全风险"
      - "什么是可以接受的技术债"
  step2_machineAlignment:
    goal: "把共识固化为 AI 可执行约束"
    outputs:
      - Rule
      - Skill
      - SOP
      - GatePolicy
```

这个顺序很朴素，但非常关键。

> 人没对齐，AI 只会把分歧自动化。

## Rule：把硬约束变成默认执行

Rule 适合表达硬约束。

也就是那些“不应该靠模型自由发挥”的东西。

比如：

- 禁止 Controller 直接访问数据库。
- 禁止明文 AK/SK。
- 对外接口不能返回内部异常堆栈。
- Application 层不能依赖 Infrastructure 实现。
- 核心链路库存扣减必须使用原子条件更新。
- 新增高危安全问题必须阻断。

Rule 的特点是明确、可判断、可阻断。

它不应该写得太虚。

比如“代码要优雅”“架构要合理”“注意安全”都不是好 Rule。

因为它们没有清晰判断条件。

好的 Rule 应该能回答四个问题：

- 检查对象是什么？
- 触发条件是什么？
- 严重等级是什么？
- 触发后怎么处理？

一个 Rule 可以这样设计：

```yaml
rule:
  id: architecture-controller-no-db
  name: "Controller 不允许直接访问数据库"
  scope:
    include:
      - "src/main/java/**/controller/**/*.java"
  condition:
    forbiddenDependencies:
      - "Mapper"
      - "Repository"
      - "JdbcTemplate"
  severity: BLOCKER
  gateAction: BLOCK
  evidenceRequired: true
  humanException:
    allowed: true
    requiresReason: true
    expiresInDays: 30
```

这里面有几个关键点。

第一，Rule 要有 scope。

不是所有规则都适用于所有代码。

第二，Rule 要有 condition。

否则模型只能凭感觉判断。

第三，Rule 要有 severity 和 gateAction。

这样它才能进入 Pre-PR Gate，而不是停留在建议层。

第四，Rule 要支持例外，但例外必须留痕。

因为工程治理不是绝对禁止一切，而是让风险可解释、可追踪。

配图提示词：

> 一张 Rule 结构拆解图，中心是一个 Rule 卡片，周围展开 scope、condition、severity、gateAction、evidenceRequired、humanException 六个字段，并连接到 Pre-PR Gate 的 BLOCK / HUMAN REVIEW / ADVISORY 状态。风格现代、清晰、适合公众号正文配图，不要 Logo，不要水印。

## Skill：把专业能力变成可复用模块

Rule 管硬约束。

Skill 管专业能力。

这两者很容易混在一起，但它们不是一回事。

Rule 更像“红线”。

Skill 更像“方法”。

比如安全审查里：

Rule 可以写：

> 禁止明文密钥出现在代码中。

Skill 则要知道：

- 哪些字符串像密钥。
- 哪些配置文件需要重点检查。
- 哪些框架容易泄露敏感字段。
- 权限绕过通常出现在哪些路径。
- 风险证据应该如何描述。

再比如异常处理：

Rule 可以写：

> 对外接口不能直接返回内部异常堆栈。

Skill 则要知道：

- 异常是否被吞掉。
- 是否缺少错误码。
- 是否破坏调用方语义。
- 是否需要重试、降级或补偿。
- 这个异常分支是否影响核心链路。

所以 Skill 更适合承载“专家经验”。

它不一定每次都阻断，但它能提高 Agent 的判断质量。

项目里已经出现了几类典型 Skill：

```yaml
skills:
  SecuritySkill:
    role: "安全审计"
    focus:
      - "敏感信息泄露"
      - "鉴权绕过"
      - "注入风险"
      - "权限边界"
  PerformanceSkill:
    role: "性能分析"
    focus:
      - "循环查库"
      - "慢查询"
      - "缓存误用"
      - "资源释放"
  JavaCodeStyleSkill:
    role: "代码规范"
    focus:
      - "命名"
      - "分层"
      - "可读性"
      - "依赖方向"
  ExceptionHandlingSkill:
    role: "异常处理"
    focus:
      - "错误码"
      - "异常分支"
      - "重试降级"
      - "调用方语义"
```

Skill 的价值，是让经验可以被不同策略复用。

`quality-gate` 可以用它。

`architecture-board` 可以用它。

未来测试生成、发布检查、重构规划也可以用它。

如果经验只写在 Prompt 里，它就跟某一次模型调用绑定在一起。

如果经验变成 Skill，它就能进入平台编排。

这就是从“经验提示”到“能力模块”的变化。

## SOP：把流程经验变成可执行步骤

Rule 和 Skill 解决“怎么判断”。

SOP 解决“怎么做”。

很多团队的经验，不是一条规则，也不是一个专业能力，而是一套流程。

比如渐进式重构。

不是一句“把代码分层”就能完成。

它需要步骤：

1. 先由主 R 选择一个复杂包做打样。
2. 明确 Controller、Application、Domain、Infrastructure 的职责边界。
3. 定义迁移前后的目录结构。
4. 迁移一个典型接口。
5. 补齐测试和回归验证。
6. 记录容易踩坑的点。
7. 把步骤分发给其他成员和 AI 执行。

这就是 SOP。

它的价值在于把“一个资深工程师脑子里的做法”，变成团队可以复用的执行路径。

在 AI Coding 场景里，SOP 更重要。

因为 AI 很擅长执行明确步骤。

但它不擅长自己定义正确步骤。

一个可执行 SOP 可以长这样：

```yaml
sop:
  id: layered-refactor-migration
  name: "工程分层迁移 SOP"
  trigger: "需求迭代中发现旧包需要改造"
  steps:
    - id: identify_current_package
      human: "确认本次需求涉及的旧包和核心业务语义"
      ai: "扫描包内 Controller、Service、Repository、DTO 使用情况"
    - id: design_target_structure
      human: "确认目标分层和边界取舍"
      ai: "生成迁移建议和影响面列表"
    - id: migrate_incrementally
      human: "选择最小可验证迁移单元"
      ai: "按模板迁移代码并保留兼容入口"
    - id: verify_behavior
      human: "验收业务语义和边界条件"
      ai: "生成测试用例、检查异常分支、输出风险摘要"
  output:
    - "迁移后的代码结构"
    - "风险 Finding"
    - "测试建议"
    - "后续技术债列表"
```

这里有一个关键点：

> SOP 不是让 AI 全自动做完，而是明确人和 AI 的分工。

人负责判断业务语义、边界取舍和最终责任。

AI 负责扫描、生成、补充、校验、整理。

这也是 Human-in-the-loop 在工程场景里的真正含义。

配图提示词：

> 一张 SOP 执行流程图，以“主 R 打样 -> SOP 沉淀 -> AI 辅助迁移 -> 人工验收 -> 规则回流”为主线，每一步标注人负责什么、AI 负责什么。风格工程化、清晰、适合公众号正文配图，不要 Logo，不要水印。

## Rule / Skill / SOP 的边界

Rule、Skill、SOP 经常一起出现，但最好不要混用。

可以用一句话区分：

> Rule 管不能突破的边界，Skill 管专业判断的方法，SOP 管可重复执行的流程。

更具体一点：

```yaml
governancePrimitives:
  Rule:
    question: "什么不能做？触发后怎么处理？"
    examples:
      - "禁止 Controller 直连数据库"
      - "禁止明文密钥"
      - "BLOCKER 必须阻断"
  Skill:
    question: "如何专业地判断一个问题？"
    examples:
      - "SecuritySkill"
      - "PerformanceSkill"
      - "ExceptionHandlingSkill"
  SOP:
    question: "一件复杂工作应该按什么步骤执行？"
    examples:
      - "分层迁移 SOP"
      - "发布前检查 SOP"
      - "测试用例生成 SOP"
```

如果把 Rule 写得像 Skill，它会变得太软。

比如“关注代码可维护性”，这不是 Rule。

它更像 Skill 的审查方向。

如果把 Skill 写得像 Rule，它又会太死。

比如安全审查不能只包含“禁止明文密钥”，它还要覆盖权限、注入、敏感字段、依赖风险等专业判断。

如果把 SOP 写成一堆 Rule，它会失去流程上下文。

比如发布前检查不只是“CI 必须通过”，还包括谁确认、什么时候确认、失败怎么处理、证据如何留存。

边界清楚，平台才好编排。

## 从规则到规则包

单条 Rule 很有用。

但企业治理通常不是一条规则起作用，而是一组规则共同服务一个业务目标。

这就是规则包。

项目里的 `GovernanceRulePack` 就是这个思路。

它不是只关心“有哪些规则”。

它还关心：

- 业务结果是什么。
- 包含哪些 controls。
- 依赖哪些能力。
- 建议使用哪些 Review 策略。
- 哪些地方需要人工检查点。

比如一个安全发布规则包：

```yaml
rulePack:
  id: security-release
  name: "安全发布门禁"
  businessOutcome: "防止新增高危漏洞、密钥和权限绕过进入主干"
  controls:
    - "BLOCKER 必须阻断"
    - "MAJOR 必须安全负责人确认"
    - "敏感接口需要测试证据"
  capabilityIds:
    - quality-gate
    - security-scan
    - secret-dependency-risk
  suggestedStrategies:
    - quality-gate
    - architecture-board
  humanCheckpoints:
    - security owner sign-off
```

再比如 AI 生成代码保障规则包：

```yaml
rulePack:
  id: ai-generated-code
  name: "AI 生成代码保障"
  businessOutcome: "让 AI 生成代码满足更严格的新代码质量和安全标准"
  controls:
    - "新代码必须走质量门禁"
    - "异常处理与边界条件必须覆盖"
    - "高风险建议需要人工确认"
  suggestedStrategies:
    - quality-gate
  humanCheckpoints:
    - tech lead confirmation
```

规则包的价值，是把治理从技术规则提升到业务场景。

开发看到的不是一堆零散规则。

而是：

> 这次改动属于安全发布，所以要启用安全发布门禁。

> 这次改动主要是 AI 生成代码，所以要启用新代码保障策略。

这样治理才不会变成“规则大杂烩”。

配图提示词：

> 一张从 Rule 到 Rule Pack 再到 Workflow Template 的层级图，底层是单条 Rule 和 Skill，中间聚合成 security-release、ai-generated-code、architecture-boundary 等规则包，顶层映射到 release-gate、daily-pre-pr、architecture-board 等工作流模板。风格清晰、工程化、适合公众号正文配图，不要 Logo，不要水印。

## 从规则包到工作流模板

规则包解决“这个场景要用哪些约束”。

工作流模板解决“这些约束在什么流程里执行”。

比如日常 Pre-PR 自查：

```yaml
workflowTemplate:
  id: daily-pre-pr
  name: "日常 Pre-PR 自查"
  scenario: "低风险需求、小修复、配置变更"
  strategyId: fast-scan
  rulePackIds:
    - delivery-velocity
  integrationIds:
    - local-repository
  successMetric: "10 分钟内完成审查，减少 Reviewer 基础问题反馈"
```

再比如发布质量门禁：

```yaml
workflowTemplate:
  id: release-gate
  name: "发布质量门禁"
  scenario: "正式 PR、核心链路、安全敏感发布"
  strategyId: quality-gate
  rulePackIds:
    - security-release
    - ai-generated-code
    - compliance-evidence
  integrationIds:
    - github-checks
    - sarif-code-scanning
  successMetric: "阻断新增 BLOCKER，并沉淀安全与质量证据"
```

这时平台就不只是“有规则”。

而是有了可执行的治理模板。

不同场景选择不同模板。

模板绑定策略、规则包、集成系统和成功指标。

这才是 Rule / Skill / SOP 真正进入工程流程的方式。

## 规则也需要复盘和学习

规则不是写完就结束。

规则也会过期。

Skill 也会误判。

SOP 也会随着团队和系统演进而变化。

所以治理系统必须有反馈回路。

哪些 Finding 经常被人工确认？

这些问题是否应该沉淀为更明确的 Rule？

哪些 Finding 经常被驳回？

是模型误判，还是规则表达太宽？

哪些 BLOCKER 经常触发？

是团队违反规范，还是规则过于激进？

哪些 SOP 步骤经常失败？

是工具不够，还是人机分工不清楚？

这些数据应该反过来改进规则。

```yaml
ruleLearningLoop:
  inputs:
    - confirmedFindings
    - dismissedFindings
    - acceptedRiskRecords
    - gateBlockedReasons
    - workflowFailures
  outputs:
    - newRules
    - updatedSkills
    - refinedSopSteps
    - adjustedGatePolicy
    - deprecatedRules
```

这就是治理平台和规范文档的差别。

文档写完，通常就静静躺在那里。

治理平台会持续观察规则是否有效。

它会把团队每一次 Review、每一次阻断、每一次人工确认，都变成下一轮规则优化的材料。

这也是 AI Coding 治理长期最值钱的地方：

> 团队经验不是一次性写入，而是持续沉淀、校准和演进。

## 结尾

Rule / Skill / SOP，是 Agent 工程化里最接近“团队经验”的部分。

Rule 把硬约束变成默认执行。

Skill 把专业判断变成可复用能力。

SOP 把复杂工作变成可复制流程。

规则包把零散约束聚合成业务场景。

工作流模板把治理能力放进真实研发流程。

它们共同解决的是同一个问题：

> 如何把少数资深工程师脑子里的经验，变成整个团队和 AI 都能稳定执行的系统。

但规则、Skill、SOP 只是治理能力的一部分。

当这些能力跑起来之后，Agent 就不只会参与 Review。

它还可以参与测试、发布、重构、风险评估和技术债治理。

这就引出了下一篇：

> 从 Review 到 Workflow：Agent 如何参与发布、测试和重构？
