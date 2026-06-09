# Judge Agent：让模型审模型，但不能让模型独裁

封面图中文提示词：

> 一张公众号科技文章封面图，主题是“Judge Agent：让模型审模型，但不能让模型独裁”。画面中心是一位抽象的 AI Judge 坐在工程审查控制台前，左侧有多个 Worker 模型节点输出代码审查意见，右侧是结构化的 Finding、风险等级、置信度和人工确认入口。画面要体现“汇总、裁决、分歧识别、人工治理权”这些概念。整体风格现代、工程化、克制，深色科技背景，蓝色代码流、绿色确认标记、橙色风险提示点缀，不要水印，适合宽幅公众号封面2.35：1。

上一篇，我们讲了多模型 Review。

多模型解决的是一个很现实的问题：

> 单一模型会有稳定盲区。

所以我们让不同模型、不同角色、不同 Skill 去看同一份代码变更。

安全模型看安全。

性能模型看性能。

架构模型看架构。

长上下文模型看整体影响。

这一步做完之后，系统的风险召回率通常会变好。

但另一个问题马上出现：

> 多个模型意见不一致时，怎么办？

一个模型说这里是 BLOCKER。

另一个模型说只是 MINOR。

一个模型认为这里有并发风险。

另一个模型完全没提。

一个模型建议重构。

另一个模型认为当前实现可以接受。

如果只是把所有模型输出拼在一起，Reviewer 不会更轻松，反而会更累。

因为他面对的已经不是一份代码 diff，而是一堆相互重叠、甚至彼此冲突的模型观点。

这时就需要 Judge Agent。

但这篇文章的重点，不是说“让 Judge 替人做决定”。

恰恰相反：

> Judge Agent 的价值，是让模型审模型；但它的边界，是不能让模型独裁。

## 为什么需要 Judge？

多模型 Review 的结果，天然会带来三类问题。

第一类是重复。

不同模型可能命中同一个风险点，只是表达方式不同。

比如一个模型说：

> 库存扣减存在并发风险。

另一个模型说：

> 当前逻辑先查库存再更新库存，缺少原子条件更新。

它们说的其实是同一个问题。

如果不合并，Reviewer 会看到两条 Finding，以为问题变多了，其实只是同一个风险被重复表达。

第二类是冲突。

一个模型认为这是必须阻断的风险，另一个模型认为只是建议优化。

这种情况下，系统既不能简单取最高等级，也不能粗暴取平均值。

取最高等级，容易导致 AI 过度阻断。

取平均值，又可能稀释真正严重的问题。

第三类是噪音。

模型会输出一些看起来有道理、但证据不足的建议。

比如“建议优化性能”“建议补充测试”“建议增强健壮性”。

这些话不一定错，但如果没有文件、行号、触发证据和影响解释，就很难进入工程流程。

Judge Agent 要解决的，正是这三件事：

- 合并重复问题。
- 识别模型分歧。
- 把低证据建议降级或交给人工确认。

配图提示词：

> 一张工程流程图，左侧是多个 Worker 模型输出审查意见，中间是 Judge Agent，右侧分成四类输出：合并后的 Finding、模型分歧、低证据建议、需要人工确认的问题。整体风格清晰、克制、适合技术公众号正文配图，浅色背景，蓝绿主色，不要 Logo，不要水印。

## Worker/Judge 模式是什么？

Worker/Judge 模式并不神秘。

可以把它理解为两层审查。

第一层是 Worker。

多个 Worker 模型从不同角度审查同一份 diff，分别输出自己的判断和证据。

第二层是 Judge。

Judge 不需要从零开始重新审代码，而是基于 Worker 的输出做二次判断。

它真正要回答的是这些问题：

- 哪些问题其实是同一个问题？
- 哪些问题被多个模型交叉命中？
- 哪些问题存在模型分歧？
- 哪些问题证据不足？
- 哪些问题应该阻断？
- 哪些问题应该交给人确认？

项目里的 `architecture-board` 策略就是一个典型例子：

```yaml
strategy:
  id: architecture-board
  name: 架构委员会评审
  reviewMode: JUDGE
  recommendedFor:
    - 架构调整
    - 领域模型升级
    - 高影响范围重构
  roleBindings:
    - role: WORKER
      model: qwen-plus
      skills:
        - JavaCodeStyleSkill
        - PerformanceSkill
    - role: WORKER
      model: deepseek-v3
      skills:
        - SecuritySkill
        - ExceptionHandlingSkill
    - role: JUDGE
      model: qwen-max
      skills:
        - JudgeReviewSkill
```

这里有一个很容易被忽略的设计点：

> Judge 不是另一个更强的 Worker。

Worker 的任务，是发现问题。

Judge 的任务，是处理问题之间的关系。

Worker 更像专家评审。

Judge 更像会议主持人和风险整理者。

它不一定比所有 Worker 都“更聪明”。

它只是承担了不同职责。

配图提示词：

> 一张 Worker/Judge 双层架构图，上层是代码 Diff 输入，下层左侧有两个或三个 Worker 模型节点分别标注“规范性能”“安全异常”“架构上下文”，这些节点输出到中间的 Judge Agent，Judge 再输出统一的风险结论、冲突列表和人工复核项。画面风格现代工程化，节点清晰，适合公众号正文，不要文字过多，不要 Logo，不要水印。

## Judge 的输入不能只是自然语言

Judge 要真正发挥作用，有一个前提：

> Worker 的输出必须结构化。

如果 Worker 只是返回一大段自然语言，Judge 很难稳定处理。

更合理的方式，是让 Worker 输出标准化 Finding，而不是一段“看起来很聪明”的评论。

比如：

```json
{
  "sourceModel": "deepseek-v3",
  "role": "WORKER",
  "file": "CouponApplicationService.java",
  "lineStart": 42,
  "lineEnd": 58,
  "category": "PERFORMANCE",
  "severity": "MAJOR",
  "title": "库存扣减逻辑缺少原子更新",
  "evidence": "当前实现先查询库存再更新库存，在并发请求下可能出现超卖",
  "suggestion": "使用带 stock > 0 条件的原子 update，并检查影响行数",
  "confidence": 0.82
}
```

有了这种结构，Judge 才能稳定做聚合。

它可以判断两个 Finding 是否指向同一文件、同一代码区域、同一风险类别。

它可以比较不同模型给出的 severity。

它可以检查证据是否充分。

它也可以把多个模型的输出压缩成一个更清晰、更适合人消费的结论。

在平台数据结构里，这类信息最后应该落到几个关键字段上：

```ts
export interface ReviewFinding {
  filePath: string
  lineStart: number | null
  lineEnd: number | null
  category: 'CODE_STYLE' | 'BUG' | 'PERFORMANCE' | 'SECURITY' | 'EXCEPTION_HANDLING' | 'OTHER'
  severity: 'BLOCKER' | 'MAJOR' | 'MINOR' | 'INFO'
  title: string
  description: string | null
  suggestion: string | null
  modelName: string | null
  confidence: number | null
  isCrossHit: boolean
  humanStatus: 'PENDING' | 'CONFIRMED' | 'DISMISSED'
}
```

这里的 `confidence`、`isCrossHit`、`humanStatus` 尤其关键。

`confidence` 表示模型结论的可信度。

`isCrossHit` 表示这个问题是否被多个模型交叉命中。

`humanStatus` 表示人工最终如何处理这个问题。

也就是说，Judge 的输出，不是为了“说服人接受 AI 的结论”。

它是为了让人更容易看清：

> 哪些问题可信，哪些问题有争议，哪些问题需要我来拍板。

## Judge 应该输出什么？

一个成熟的 Judge，不应该只给一句笼统结论：

> 综合来看，本次代码存在 3 个问题。

这太粗，也无法进入后续流程。

Judge 的输出至少应该包括五类信息。

第一，归并后的 Finding。

同一个风险点只保留一条，但保留来源模型和证据。

第二，严重等级。

Judge 可以建议最终 severity，但必须说明为什么。

第三，置信度。

如果多个模型交叉命中、证据充分、影响明确，置信度可以提高。

如果只有单模型发现、证据不足、影响不清楚，置信度应该降低。

第四，冲突说明。

如果模型意见不一致，Judge 要把分歧讲清楚，而不是用一句“综合判断”把分歧抹平。

第五，人工动作建议。

Judge 最终应该给出“阻断、人工复核、建议放行、忽略”的动作建议。

一个 Judge 输出可以长这样：

```json
{
  "summary": "本次审查聚合出 4 个有效 Finding，其中 1 个 BLOCKER、2 个 MAJOR、1 个 MINOR。",
  "findings": [
    {
      "title": "库存扣减存在并发超卖风险",
      "severity": "BLOCKER",
      "confidence": 0.91,
      "isCrossHit": true,
      "sources": ["qwen-plus", "deepseek-v3"],
      "decision": "BLOCK",
      "reason": "两个 Worker 均指出同一代码区域缺少原子扣减条件，且影响核心交易链路。",
      "requiresHumanConfirmation": false
    },
    {
      "title": "异常分支缺少业务错误码",
      "severity": "MAJOR",
      "confidence": 0.68,
      "isCrossHit": false,
      "sources": ["deepseek-v3"],
      "decision": "REQUIRE_HUMAN_REVIEW",
      "reason": "存在异常处理不完整迹象，但是否影响调用方语义需要业务负责人确认。",
      "requiresHumanConfirmation": true
    }
  ],
  "modelDisagreements": [
    {
      "topic": "是否需要拆分 CouponApplicationService",
      "workerOpinions": [
        "qwen-plus 认为当前类职责偏重，建议拆分",
        "deepseek-v3 未认为这是本次必须处理的问题"
      ],
      "judgeConclusion": "作为重构建议记录为 MINOR，不阻断本次提交。"
    }
  ]
}
```

注意这里的措辞。

Judge 给的是 `decision` 和 `reason`。

但它也会标出 `requiresHumanConfirmation`。

这就把“模型判断”和“治理决策”分开了。

## Judge 不能做什么？

Judge Agent 最容易被误用的地方，是被当成最终裁判。

这听起来很诱人，也很危险。

多个模型吵不明白，那就再找一个更强的模型来拍板。

问题是，工程治理不是辩论赛。

模型判断再完整，也无法替代团队标准、业务语义和最终责任。

Judge 至少不能做三件事。

第一，不能替代团队标准。

团队没有明确规则时，Judge 只能基于通用经验判断。

但通用经验不等于团队标准。

比如某个项目允许 Controller 做轻量编排，另一个项目严格禁止 Controller 直接触碰业务逻辑。Judge 如果不知道团队规范，就很容易把“约定差异”误判成“质量问题”。

第二，不能替代业务语义。

模型可以看代码路径、异常分支、数据流，但它未必知道一个业务规则背后的真实约束。

比如某个接口为什么允许重复提交，某个字段为什么不能立刻删除，某个补偿任务为什么必须保留幂等冗余。

这些判断最终要回到业务负责人和技术负责人。

第三，不能替代最终责任。

一旦 Judge 可以自动决定所有问题，团队很容易把责任外包给模型。

这会带来一个危险倾向：

> 模型没拦住，所以不是人的问题。

这在工程治理里是不能接受的。

AI 可以参与判断，但不能成为责任主体。

配图提示词：

> 一张“Judge 能做什么 / 不能做什么”的对照图，左侧是 Judge 能做的事：合并 Finding、识别冲突、提升置信度、建议动作；右侧是 Judge 不能做的事：替代团队标准、替代业务语义、替代最终责任。整体风格清晰、理性、工程化，适合技术公众号正文配图，浅色背景，蓝色和橙色对比，不要 Logo，不要水印。

## 人类最终治理权要如何保留？

如果 Judge 不能独裁，系统应该怎么设计？

关键是把 AI 结论和人工决策拆开。

AI 可以给出：

- 风险等级建议。
- 证据链。
- 置信度。
- 是否交叉命中。
- 是否建议阻断。
- 是否建议人工复核。

但最终状态必须允许人处理：

- 确认问题。
- 驳回问题。
- 接受风险。
- 要求修改。
- 转为后续技术债。

一个成熟的人工确认记录可以这样设计：

```yaml
humanDecision:
  findingId: F-20260605-017
  aiDecision: REQUIRE_HUMAN_REVIEW
  humanStatus: CONFIRMED
  decidedBy: tech-lead-a
  reason: "该问题影响核心交易链路，必须在本次提交前修复"
  decidedAt: "2026-06-05T15:30:00+08:00"
```

也可以是接受风险：

```yaml
humanDecision:
  findingId: F-20260605-021
  aiDecision: REQUIRE_HUMAN_REVIEW
  humanStatus: DISMISSED
  decidedBy: reviewer-b
  reason: "该接口仅用于内部灰度脚本，本次接受日志上下文不足问题，下个迭代统一补齐"
  expiresAt: "2026-07-01"
```

这里有一个原则很重要：

> 人可以推翻 AI，但必须留下理由。

这不是为了增加流程负担。

而是为了给团队留下复盘材料。

如果某类 AI Finding 经常被驳回，说明规则、Skill 或 Prompt 可能需要调整。

如果某类人工驳回后来造成线上问题，说明团队的风险判断需要校准。

如果某个模型经常在 Judge 阶段被降级，说明它不适合承担当前角色。

这些数据会反过来改进模型策略、Rule、Skill 和 Gate Policy。

这才是治理系统的价值。

## Judge 的最佳位置：决策支持，而不是决策主体

我更倾向于把 Judge Agent 定义成四个角色。

第一，整理者。

它把多个 Worker 的输出整理成统一结构，降低 Reviewer 的阅读负担。

第二，裁剪者。

它去掉重复、低证据、泛泛而谈的建议，减少噪音。

第三，解释者。

它说明为什么某个问题严重，为什么某个问题只是建议，为什么某个问题需要人确认。

第四，提醒者。

它把模型分歧、低置信结论、业务语义不确定的地方暴露出来，提醒人介入。

但它不应该成为最终责任人。

所以一个合理的 Judge 决策链路可以是：

```yaml
judgeDecisionFlow:
  input:
    - workerFindings
    - gatePolicy
    - teamRules
    - reviewContext
  judgeActions:
    - normalize
    - deduplicate
    - detectCrossHits
    - detectDisagreements
    - recommendSeverity
    - recommendGateAction
  finalStates:
    - PASSED
    - BLOCKED
    - NEEDS_HUMAN_REVIEW
  humanAuthority:
    - confirm
    - dismiss
    - acceptRisk
    - requestChanges
```

这里的关键词是 `recommend`。

Judge 可以建议 Gate Action。

但真正进入团队治理系统时，必须保留人工确认和例外机制。

配图提示词：

> 一张 Judge 决策链路图，左侧输入包括 Worker Findings、Gate Policy、Team Rules、Review Context，中间是 Judge Actions：归一化、去重、交叉命中、分歧识别、严重等级建议、Gate 动作建议，右侧是最终状态 PASSED、BLOCKED、NEEDS_HUMAN_REVIEW，并在最右侧保留 Human Authority 人工确认入口。风格现代、工程化、结构清晰，适合公众号正文配图，不要 Logo，不要水印。

## 落到系统里，Judge 要留下什么？

如果只是在一次 Review 里调用 Judge，然后把结果展示出来，这个能力还不算真正平台化。

真正的平台化，意味着 Judge 的每一次判断都要能被追踪、复盘和修正。

至少要留下五类数据。

第一，Worker 原始输出。

Judge 不能把 Worker 的观点直接吞掉。否则后面一旦发生误判，团队很难知道问题出在 Worker、Judge，还是规则本身。

所以每个 Finding 都应该能追溯到来源模型、来源角色、来源 Skill 和原始证据。

第二，Judge 归并过程。

两个 Finding 为什么被认为是同一个问题？

一个问题为什么从 MAJOR 升级到 BLOCKER？

一个建议为什么被降级成 INFO？

这些都应该留下可解释的理由，而不是只留下一个最终等级。

第三，Gate 动作建议。

Judge 可以建议 `BLOCK`、`REQUIRE_HUMAN_REVIEW`、`ADVISORY` 或 `PASS`。

但这个建议最好和团队 Gate Policy 分开记录。

模型判断是模型判断。

策略执行是策略执行。

这样后续调整规则时，团队才能区分：到底是模型误判，还是当前策略过严或过松。

第四，人工处理结果。

人确认了什么，驳回了什么，接受了哪些风险，都应该回写到 Finding 上。

这些不是普通表单字段，而是后续治理优化的反馈数据。

如果某个 Worker 经常提出被驳回的安全问题，说明它的 Skill 可能过宽。

如果某个 Judge 经常把后来被确认的 BLOCKER 降级，说明 Judge 策略需要收紧。

第五，复盘指标。

Judge Agent 不能只看一次输出是否“像样”。

它应该长期看几个指标：

- 重复 Finding 的合并率。
- 交叉命中的确认率。
- 人工驳回率。
- BLOCKER 的误报率。
- MAJOR 的漏报回溯。
- 不同模型在不同角色下的有效命中率。

这些指标会慢慢告诉团队：哪些模型适合做 Worker，哪些模型适合做 Judge，哪些 Skill 需要重写，哪些 Gate Policy 需要调整。

这也是 Judge Agent 和普通“总结一下多模型结果”的差别。

普通总结只是把输出变短。

Judge Agent 要把输出变成可治理的数据。

配图提示词：

> 一张平台数据闭环图，主题是 Judge Agent 如何沉淀治理数据。画面左侧是 Worker 原始输出，中间是 Judge 归并、分歧识别、严重等级建议和 Gate 动作建议，右侧是人工确认、驳回、接受风险等处理结果；底部形成复盘指标面板，包括合并率、确认率、误报率、漏报回溯、模型角色命中率，最后反馈回模型策略、Skill 和 Gate Policy。整体风格现代工程化、克制、适合技术公众号正文配图，蓝色主流程，绿色表示确认闭环，橙色表示风险和校准，不要 Logo，不要水印。

## Judge Agent 的真正价值

Judge Agent 的价值，不是让 AI 变成裁判。

它真正解决的是三个问题。

第一，把多模型输出变成可消费结论。

多个 Worker 模型分别输出观点，如果没有 Judge，Reviewer 面对的是信息堆叠。有了 Judge，Reviewer 面对的是整理后的风险视图。

第二，把模型分歧显性化。

系统不应该掩盖分歧。恰恰相反，分歧是最值得人关注的地方。

如果两个模型对同一个问题严重等级判断不同，这可能说明证据不足、规则不清、业务上下文缺失，或者风险确实处在灰区。

第三，把治理过程数据化。

Judge 的每一次归并、降级、升级、冲突识别、人工确认，都会成为后续优化策略的依据。

这让 AI Review 不再是一次性模型调用，而是一个会持续校准的工程系统。

换句话说：

> Judge Agent 不是为了替代 Reviewer，而是为了让 Reviewer 更快看见真正需要判断的地方。

## 结尾

多模型 Review 带来了更高召回率，也带来了更多观点。

如果没有 Judge，这些观点会堆成噪音。

如果 Judge 被滥用，它又会变成模型独裁。

所以 Judge Agent 的正确位置很微妙：

> 它应该让模型审模型，但不能让模型替团队负责。

它负责汇总、归并、识别冲突、提出决策建议。

人负责确认、驳回、接受风险和承担最终治理责任。

这也是 AI 工程治理平台和普通 AI Review 工具的分水岭。

工具只关心模型能不能找问题。

平台还要关心问题如何被解释、被确认、被复盘、被沉淀。

下一篇，我们继续往 Agent 内部走一层：

> Agent 不是 Prompt：Role、Skill、Tool、Memory 缺一不可。
