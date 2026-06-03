# AI Coding 时代的 Agent 工程化治理：公众号系列大纲

## 系列定位

建议不要把这个系列写成普通的“Agent 技术教程”，而是写成：

> AI Coding 时代，如何把团队经验、工程规范、质量门禁和研发流程变成可执行的 Agent 治理系统。

项目最有价值的地方不是“用 AI 做 Code Review”，而是把 Review、Pre-PR Gate、多模型策略、Judge、Rule、Skill、Workflow、MCP、知识记忆这些能力串成一套研发治理系统。

## 目标读者

- 技术负责人
- 架构师
- AI 工程化实践者
- 正在使用 AI Coding 的研发团队
- 想把 AI 从“个人效率工具”推进到“团队工程基础设施”的团队

## 核心主张

> AI Coding 提升了写代码速度，但也放大了工程失控风险。Agent 真正有价值的地方，不是替人写代码，而是把团队经验、工程规范、质量门禁和研发流程变成可执行系统。

## 三条主线

1. 认知线：为什么 AI Coding 需要治理。
2. 系统线：一个工程 Agent 平台应该怎么设计。
3. 落地线：Pre-PR、Review、测试、发布、知识沉淀如何一步步接入。

## 连载总览

建议写 10 篇，节奏是：先讲问题，再讲平台，再讲核心模块，最后讲演进。

| 篇数 | 标题 | 核心问题 | 项目素材 |
|---|---|---|---|
| 1 | 当 90% 代码由 AI 生成，研发治理会发生什么变化？ | AI Coding 为什么不是单纯提效，而是带来新的复杂度 | `doc/设计.md`、平台定位文档 |
| 2 | 为什么 Code Review 会成为 AI Coding 之后的新瓶颈？ | AI 写得快，人审不过来怎么办 | Review Center、Finding、Review Timeline |
| 3 | Pre-PR Gate：把 AI Review 前置到提交之前 | 为什么要在 PR 前设置质量门禁 | `pre_pr_gate`、Gate Policy |
| 4 | 多模型 Review：为什么一个模型不够？ | 多模型交叉审查、不同模型角色分工 | `modelStrategies.ts`、`model_provider`、`model_profile` |
| 5 | Judge Agent：让模型审模型，但不能让模型独裁 | Judge 的价值和边界 | `JUDGE` 策略、Worker/Judge 配置 |
| 6 | Agent 不是 Prompt：Role、Skill、Tool、Memory 缺一不可 | 工程 Agent 的组成结构 | Agent Runtime、SkillRegistry、Agent tools |
| 7 | MCP 和工具层：Agent 如何真正“进入工程现场”？ | Agent 需要读代码、看 Git、跑工具，而不是只聊天 | MCPToolBridge、GitTool、ReadFileTool、SaveFindingTool |
| 8 | Rule / Skill / SOP：把团队经验变成 AI 可执行约束 | 如何从“文档规范”变成“执行系统” | Rule Engine、SOP Compiler、Skill |
| 9 | 从 Review 到 Workflow：Agent 如何参与发布、测试和重构？ | Agent 平台如何扩展到研发全流程 | ReleaseWorkflow、TestGeneration、RefactorPlanner |
| 10 | 从 AI Review 工具到 AI Engineering Governance Platform | 一年演进路线和平台最终形态 | 治理中心、知识图谱、架构大脑、运营看板 |

## 每篇大纲

### 第 1 篇：当 90% 代码由 AI 生成，研发治理会发生什么变化？

开篇钩子：

> AI Coding 没有自动降低复杂度，它只是让复杂度增长得更快。

结构：

1. AI Coding 带来的真实变化：写代码成本下降，审查成本上升。
2. 为什么“个人效率提升”会变成“团队系统风险”。
3. 传统研发治理的问题：规范靠文档，执行靠人盯。
4. 新范式：人先对齐标准，再让 AI 执行标准。
5. 引出系列：我们需要的不是 AI Review 工具，而是 AI 工程治理平台。

金句：

> AI Coding 时代，工程师的核心能力不再只是写代码，而是设计一个能让 AI 稳定产出好代码的工程环境。

### 第 2 篇：为什么 Code Review 会成为 AI Coding 之后的新瓶颈？

结构：

1. AI 把编码环节压缩了，但 Review 没有同步提速。
2. 人工 CR 应该从“查语法和规范”转向“判断业务语义和架构方向”。
3. AI Review 应该负责什么：规范、异常处理、安全、性能、明显缺陷。
4. 人应该负责什么：业务正确性、方案合理性、边界取舍。
5. Review Agent 平台的第一层价值：过滤低价值问题，让人聚焦高价值判断。

### 第 3 篇：Pre-PR Gate：把 AI Review 前置到提交之前

结构：

1. 为什么正式 PR 不是最好的 AI Review 时机。
2. Pre-PR 的核心流程：提交前自查、修复、生成 PR 摘要、再进入人工 Review。
3. Gate 状态设计：PASSED、BLOCKED、NEEDS_HUMAN_REVIEW。
4. 严重等级设计：BLOCKER、MAJOR、MINOR、INFO。
5. 关键原则：AI 可以阻断基础风险，但最终治理权仍要留给人。

### 第 4 篇：多模型 Review：为什么一个模型不够？

结构：

1. 单模型 Review 的问题：盲区稳定存在。
2. 多模型不是堆模型，而是做角色分工。
3. 示例策略：快速自查、交叉审查、Pre-PR 质量门禁、架构委员会。
4. 模型能力标签：长上下文、复杂推理、安全、代码理解、成本均衡。
5. 多模型治理的本质：用差异化能力提高召回率和可信度。

### 第 5 篇：Judge Agent：让模型审模型，但不能让模型独裁

结构：

1. Judge 的价值：汇总、裁决、识别冲突、提升结论一致性。
2. Worker/Judge 模式：多个审查模型先给意见，Judge 做二次判断。
3. Judge 不能做什么：不能替代团队标准，不能替代人类最终责任。
4. 如何设计 Judge 输出：证据、风险等级、置信度、是否需要人工确认。
5. 最佳实践：Judge 是决策支持，不是决策主体。

### 第 6 篇：Agent 不是 Prompt：Role、Skill、Tool、Memory 缺一不可

结构：

1. 为什么只写 Prompt 很快会失控。
2. 一个工程 Agent 的最小组成：Role、Skill、Tool、Memory、Rule、Workflow。
3. Role：安全审计、性能分析、架构评审、异常处理。
4. Skill：把专业经验变成可复用能力。
5. Tool：让 Agent 能读代码、查 Git、保存 finding。
6. Memory：让团队经验可沉淀，而不是每次重新开始。

### 第 7 篇：MCP 和工具层：Agent 如何真正进入工程现场？

结构：

1. 没有工具的 Agent 只能“评论”，有工具的 Agent 才能“工作”。
2. 工程现场需要哪些工具：Git diff、文件读取、代码结构、日志、CI、Issue。
3. MCP 的价值：统一工具协议，让 Agent 能扩展到不同系统。
4. 工具调用的风险：权限、审计、上下文污染、结果可信度。
5. 企业场景里的关键点：工具不是越多越好，而是要可控、可追踪、可治理。

### 第 8 篇：Rule / Skill / SOP：把团队经验变成 AI 可执行约束

结构：

1. 文档规范为什么经常失效。
2. “人人对齐”到“人机对齐”的顺序不能反。
3. Rule：硬约束，例如禁止 Controller 直连 DB。
4. Skill：软能力，例如异常处理、性能分析、安全审查。
5. SOP：流程约束，例如发布前必须完成哪些检查。
6. 最终目标：团队经验结构化、规则化、可执行化。

### 第 9 篇：从 Review 到 Workflow：Agent 如何参与发布、测试和重构？

结构：

1. Review 只是入口，真正的价值在研发流程。
2. 发布工作流：Gate、阻断、人工确认、发布状态。
3. 测试生成：人定范围和风险，AI 补充用例和步骤。
4. 重构规划：AI 负责扫描和拆解，人负责判断优先级。
5. 风险预测：把变更影响从“感觉”变成可解释信号。
6. Agent 平台的演进方向：从单点工具到流程执行者。

### 第 10 篇：从 AI Review 工具到 AI Engineering Governance Platform

结构：

1. 平台最终定位：AI Engineering Governance Platform。
2. 五层架构：治理门户、治理层、Agent Runtime、Tool/MCP、CI/CD。
3. 四阶段路线：Review MVP、治理平台、Workflow 平台、企业研发 Agent 基础设施。
4. 真正的资产：不是 Prompt，而是工程知识治理。
5. 结尾：未来企业缺的不是 AI，而是能约束 AI 的工程系统。

## 推荐发布节奏

建议每周 2 篇，5 周发完。

| 周次 | 主题 | 文章 |
|---|---|---|
| 第 1 周 | 问题意识 | 第 1 篇、第 2 篇 |
| 第 2 周 | 质量门禁 | 第 3 篇、第 4 篇 |
| 第 3 周 | Agent 架构 | 第 5 篇、第 6 篇 |
| 第 4 周 | 工具和治理 | 第 7 篇、第 8 篇 |
| 第 5 周 | 流程和平台化 | 第 9 篇、第 10 篇 |

## 栏目名备选

推荐第一个，比较稳，也更像一个可持续专栏。

1. AI Coding 治理笔记
2. Agent 工程化实战
3. 从 Code Review 到 Agent 平台
4. AI 原生研发治理
5. 工程 Agent 平台手记

## 第一篇建议标题

最适合作为开篇的是：

> 当 90% 代码由 AI 生成，研发治理会发生什么变化？

这个标题有冲突感，也能自然引出项目的完整价值：从 Review Agent 到 AI 研发治理平台。

## 后续写作建议

后续可以按下面的粒度推进：

1. 先写第 1 篇正文，建立整个系列的问题意识。
2. 每篇控制在 1800-2600 字，适合公众号阅读。
3. 每篇保留一个可复用框架，例如 Gate Policy、Worker/Judge、Role/Skill/Tool/Memory。
4. 技术细节不要一上来堆源码，而是从工程问题切入，再落到平台设计。
5. 每篇结尾都埋一个下一篇的问题，让连载感更强。
