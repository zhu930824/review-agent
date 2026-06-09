# MCP 和工具层：Agent 如何真正进入工程现场？

封面图中文提示词：

> 一张公众号科技文章封面图，主题是“MCP 和工具层：Agent 如何真正进入工程现场？”。画面中心是一个工程 Agent，通过 MCP Tool Bridge 连接 Git 仓库、代码文件、CI 流水线、Issue 系统、日志平台和 Review Finding 数据库。画面要体现“从聊天到工作”“工具调用”“权限审计”“工程现场”这些概念。整体风格现代、工程化、克制，深色科技背景，蓝色连接线、绿色工具节点、橙色权限审计提示点缀，不要文字、不要 Logo、不要水印，适合宽幅公众号封面。

上一篇我们讲了 Agent 的内部结构。

一个工程 Agent 不能只有 Prompt。

它至少需要 Role、Skill、Tool、Memory、Rule、Workflow。

这篇我们专门展开其中最容易被低估的一层：

> Tool 和 MCP。

为什么它重要？

因为没有工具的 Agent，只能“评论”。

有了工具的 Agent，才可能真正“工作”。

一个只靠上下文窗口的 Review Agent，看起来可以分析代码，但它看到的只是你复制给它的那一小段内容。

它不知道这个文件周围还有什么。

不知道这个方法是谁调用的。

不知道这段代码最近为什么改过。

不知道 CI 有没有失败。

不知道同类问题过去是否出现过。

也不知道它发现的问题应该保存到哪里。

这样的 Agent，更像一个坐在会议室里的顾问。

它可以发表意见。

但它没有进入现场。

真正的工程 Agent，必须能读代码、查 Git、看 CI、写 Finding、同步 Issue、触发流程。

这就是工具层的意义。

## 没有工具的 Agent，只能停留在评论层

很多 AI Review 的早期形态，大概是这样：

把 diff 复制出来。

丢给模型。

模型输出一段 Review 建议。

人再把建议搬回 PR。

这个流程可以验证模型能力，但很难支撑团队治理。

因为它有几个天然限制。

第一，上下文不完整。

模型只能看到输入里给它的内容。如果 diff 里只改了一个方法，但风险来自调用链、配置、数据库约束或历史兼容逻辑，模型很容易漏判。

第二，结果不落地。

模型说“这里有风险”，但这个风险没有进入 Finding 表，没有进入 Review Timeline，没有进入 Gate Policy，也没有进入后续统计。

第三，动作不可追踪。

谁调用了模型？

模型读了哪些文件？

它根据什么证据判断？

它有没有访问不该访问的仓库？

如果这些都没有记录，就很难在企业场景里放心使用。

第四，无法进入流程。

它不能把 BLOCKED 状态写回 GitHub/GitLab Checks，不能生成 SARIF，不能同步 Issue，也不能触发人工确认。

所以，没有工具的 Agent，最多是“智能评论员”。

它离工程基础设施还差一层。

配图提示词：

> 一张对比图，左侧是“无工具 Agent”，只能接收用户复制的 diff 并输出自然语言评论；右侧是“有工具 Agent”，可以读取 Git diff、文件上下文、代码结构、CI 状态，并把 Finding 写回治理平台。整体风格清晰、工程化、适合公众号正文配图，浅色背景，蓝绿主色，不要 Logo，不要水印。

## 工程现场到底需要哪些工具？

如果把 Code Review 放到真实研发流程里看，Agent 至少需要六类工具。

第一类是 Git 工具。

它要知道源分支和目标分支之间发生了什么变化。

不是只拿一段 diff 文本，而是能稳定获取：

- 变更文件列表。
- 每个文件的 diff hunk。
- commit 信息。
- 作者和时间。
- 相关历史修改。
- 与目标分支的差异范围。

第二类是文件读取工具。

很多风险不在 diff 行本身，而在上下文里。

比如新增一行调用，看起来没问题，但被调用方法里有事务边界、缓存行为或异常转换逻辑。

这时 Agent 必须能读取周边文件。

第三类是代码结构工具。

只读文本还不够。

Agent 最好能知道类、方法、依赖、调用关系、包结构和分层边界。

否则它很难判断“Controller 是否越界”“Application 是否依赖 Infrastructure”“循环调用是否会造成性能问题”。

第四类是 CI 和测试工具。

代码风险不只来自静态 diff。

CI 是否失败、哪些测试失败、覆盖率是否下降、静态扫描是否报警，都会影响 Review 判断。

第五类是结果写回工具。

Agent 发现问题之后，要能保存成结构化 Finding。

如果只是输出 Markdown，这些问题就无法被分级、确认、驳回、统计和复盘。

第六类是外部协作工具。

企业流程里，问题可能需要同步到 Issue、PR 评论、Commit Status、SARIF、发布看板。

这时 Agent 不只是“发现问题”，还要把问题送到正确位置。

在这个项目里，工具层已经有很清晰的雏形：

```yaml
agentTools:
  GitLogTool:
    purpose: "查看相关文件的历史变更和提交记录"
  ReadFileTool:
    purpose: "读取代码文件和周边上下文"
  CodeStructureTool:
    purpose: "分析类、方法、依赖和结构边界"
  SaveFindingTool:
    purpose: "把审查结果保存为结构化 Finding"
  MCPToolBridge:
    purpose: "把外部工具接入 Agent Pipeline"
```

这些工具组合起来，Agent 才能从“读一段文本”升级为“进入工程现场”。

配图提示词：

> 一张工程 Agent 工具地图，中心是 Agent Runtime，周围六类工具：Git 工具、文件读取、代码结构、CI/测试、结果写回、外部协作；每类工具连接对应工程系统。风格现代、清晰、克制，适合技术公众号正文配图，不要 Logo，不要水印。

## MCP 的价值：统一工具协议

工具一多，就会遇到另一个问题：

> 每接一个系统，都要重新适配一套工具协议。

GitHub 一套接口。

GitLab 一套接口。

Jira 一套接口。

Jenkins 一套接口。

内部代码搜索一套接口。

日志平台一套接口。

知识库又是一套接口。

如果每个 Agent 都直接对接这些系统，很快就会失控。

工具调用会散落在不同代码里。

权限边界会不清楚。

审计日志会不统一。

错误处理方式也会不一致。

MCP 的价值，就是在 Agent 和外部工具之间加一层标准化协议。

可以把它理解成：

> Agent 不直接认识所有系统，它认识一套工具协议；不同系统通过 MCP Server 暴露能力。

一个简化结构可以这样看：

```yaml
mcpToolLayer:
  agentRuntime:
    calls:
      - toolName: read_file
      - toolName: git_log
      - toolName: code_search
      - toolName: save_finding
  mcpBridge:
    responsibilities:
      - "工具注册"
      - "参数校验"
      - "权限控制"
      - "调用审计"
      - "结果标准化"
  mcpServers:
    - name: git-server
      tools:
        - git_diff
        - git_log
    - name: code-server
      tools:
        - read_file
        - code_structure
    - name: delivery-server
      tools:
        - ci_status
        - create_issue
        - update_check
```

这层抽象非常重要。

因为 Agent 平台不是一次性工具。

它会不断接入新的工程系统。

今天接 Git。

明天接 CI。

后天接 Issue。

再往后接日志、监控、知识库、制品仓库、发布系统。

如果没有统一工具协议，平台会被集成复杂度拖垮。

配图提示词：

> 一张 MCP 工具协议架构图，左侧是多个 Agent，中间是 MCP Tool Bridge，右侧是多个 MCP Server：Git、Code、CI、Issue、Knowledge、Log。箭头体现 Agent 通过统一协议调用不同工程系统，桥接层包含权限、审计、参数校验、结果标准化。风格工程化、层次清晰，适合公众号正文配图，不要 Logo，不要水印。

## 工具调用不能没有边界

工具让 Agent 变强，也让 Agent 变危险。

一个只能聊天的模型，最多说错话。

一个能调用工具的 Agent，可能读错文件、写错状态、误发 Issue、误阻断 PR，甚至访问不该访问的数据。

所以工具层最重要的不是“接得多”。

而是“管得住”。

企业场景里，工具调用至少要有五条边界。

第一，权限边界。

Agent 不能默认拥有所有权限。

安全审计 Agent 可能需要读取代码，但不一定需要修改 Issue。

Pre-PR Gate 可以写入 Gate 状态，但不一定能直接合并代码。

发布 Agent 可以读取 CI 状态，但发布动作必须经过人确认。

第二，范围边界。

Agent 能读哪些项目、哪些目录、哪些分支，要有明确限制。

比如某个 Agent 只能读取当前 review 关联项目，不能横向读取其他仓库。

第三，动作边界。

读操作、写操作、触发操作要分级。

读文件和写 Finding 是两种风险。

更新 PR Check 和创建 Issue 又是另一种风险。

第四，审计边界。

每一次工具调用都应该留下记录：

- 谁触发了 Agent。
- Agent 调用了哪个工具。
- 参数是什么。
- 访问了哪些资源。
- 结果摘要是什么。
- 是否成功。

第五，人工确认边界。

高风险动作必须保留人工确认。

比如自动创建修复 PR、更新分支保护状态、触发发布流程，都不应该让 Agent 默认独立完成。

可以用一个简单策略表达：

```yaml
toolPolicy:
  read:
    allowed:
      - GitDiffTool
      - ReadFileTool
      - GitLogTool
      - CodeStructureTool
    approvalRequired: false
  writeLowRisk:
    allowed:
      - SaveFindingTool
      - ExportSarifTool
    approvalRequired: false
  writeHighRisk:
    allowed:
      - UpdateCommitStatusTool
      - CreateIssueTool
      - ApplyPatchTool
    approvalRequired: true
  forbidden:
    - MergeBranchTool
    - DeleteRepositoryTool
```

这不是保守。

这是工程系统的基本卫生。

Agent 越有能力，越要有边界。

## 工具结果也需要可信度

还有一个经常被忽略的问题：

> 工具返回的结果，不等于绝对事实。

Git diff 可能取错范围。

文件读取可能读到旧版本。

代码结构分析可能不完整。

CI 状态可能还在运行中。

Issue 数据可能滞后。

日志查询可能只覆盖部分时间窗口。

如果 Agent 把所有工具结果都当成绝对事实，就会做出错误判断。

所以工具结果也要带元信息。

比如：

```json
{
  "tool": "GitDiffTool",
  "status": "SUCCESS",
  "sourceBranch": "feature/coupon-refactor",
  "targetBranch": "main",
  "baseCommit": "a1b2c3",
  "headCommit": "d4e5f6",
  "fileCount": 8,
  "truncated": false,
  "generatedAt": "2026-06-05T16:20:00+08:00"
}
```

如果结果被截断，就要告诉 Agent。

如果 CI 还没跑完，也要告诉 Agent。

如果代码结构分析失败，不能让 Agent 假装已经分析过。

工具层输出越透明，Agent 的判断越可靠。

这也是为什么工具调用需要标准化。

不只是为了“能调通”。

更是为了让 Agent 知道：

> 我拿到的信息有多完整，有多新，有多可信。

## 从 Tool 到 Workflow：工具要进入闭环

工具调用如果只是零散发生，价值仍然有限。

真正的工程化，是把工具接入 Workflow。

以 Pre-PR Gate 为例，一个完整流程可能是：

```yaml
prePrToolWorkflow:
  steps:
    - name: collect_diff
      tool: GitDiffTool
      output: DiffContext
    - name: enrich_context
      tools:
        - ReadFileTool
        - CodeStructureTool
        - GitLogTool
      output: ReviewContext
    - name: run_agents
      runtime: AgentPipeline
      output: WorkerFindings
    - name: save_findings
      tool: SaveFindingTool
      output: StructuredFindings
    - name: update_gate
      tool: UpdateCommitStatusTool
      output: PASSED_OR_BLOCKED
    - name: export_report
      tools:
        - ExportSarifTool
        - GeneratePrSummaryTool
      output: ReviewReport
```

这时 Agent 就不再是“问一下模型怎么看”。

它变成了一个流程节点。

它可以被触发。

可以被观测。

可以失败重试。

可以被审计。

可以被人工接管。

也可以沉淀数据。

这才是工具层真正的价值。

配图提示词：

> 一张 Pre-PR Gate 工具闭环流程图，从 collect diff、enrich context、run agents、save findings、update gate、export report 六步展开，每一步标注对应工具，最后输出 PASSED/BLOCKED/NEEDS_HUMAN_REVIEW。整体风格工程流程图，清晰、克制、适合公众号正文配图，不要 Logo，不要水印。

## 企业场景里，工具不是越多越好

很多平台做 Agent 工具层时，会有一种冲动：

能接什么就接什么。

Git 接上。

CI 接上。

日志接上。

Issue 接上。

知识库接上。

监控接上。

发布系统也接上。

看起来很强。

但工具越多，风险也越多。

上下文污染会变多。

权限配置会变复杂。

调用链会变长。

错误排查会变难。

模型也可能被无关工具结果带偏。

所以企业里的工具层设计，重点不是数量，而是治理。

我更建议按三个阶段推进。

第一阶段，只接只读工具。

让 Agent 能稳定读 Git diff、读文件、查代码结构、看 Git log。

这一阶段的目标是提高 Review 质量。

第二阶段，接低风险写入工具。

比如保存 Finding、导出 SARIF、生成 PR Summary。

这一阶段的目标是让结果进入工程系统。

第三阶段，接高风险流程工具。

比如更新 Commit Status、创建 Issue、触发发布检查、生成修复草案。

这一阶段必须配合权限、审计和人工确认。

换句话说：

> 先让 Agent 看得准，再让 Agent 写得稳，最后才让 Agent 参与流程状态。

这个顺序不能反。

## MCP 和工具层的真正价值

MCP 和工具层的价值，不是“让 Agent 多几个插件”。

它真正解决的是三个问题。

第一，让 Agent 拿到真实上下文。

代码、Git、CI、Issue、历史 Finding、团队规则，这些才是工程判断所需要的上下文。

第二，让 Agent 的结果进入系统。

Finding、Gate 状态、PR Summary、SARIF、Issue，这些才是工程流程能消费的产物。

第三，让工具调用可治理。

权限、范围、审计、人工确认、结果可信度，这些决定了 Agent 能不能在企业里被放心使用。

所以，MCP 不是炫技点。

它更像 Agent 平台的工程接口层。

没有它，Agent 只能停留在“我觉得这里有问题”。

有了它，Agent 才能变成：

> 我读取了这次变更，检查了相关上下文，保存了结构化 Finding，并把需要人工确认的风险推到了正确流程里。

这就是从聊天到工作的变化。

## 结尾

Agent 真正进入工程现场，不是因为模型更聪明。

而是因为它能通过工具连接真实系统。

它能读代码。

能查 Git。

能看 CI。

能保存 Finding。

能同步 Issue。

能把 Gate 状态写回交付链路。

但这也意味着，工具层必须被治理。

权限要清楚。

范围要清楚。

审计要清楚。

高风险动作要有人确认。

这一步做好之后，Agent 才真正从“会说”变成“能做”。

不过，工具只是进入现场的方式。

进入现场之后，Agent 还需要知道什么能做、什么不能做、什么必须阻断、什么可以例外。

这就引出了下一篇：

> Rule / Skill / SOP：把团队经验变成 AI 可执行约束。
