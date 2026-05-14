# 多智能体架构升级 Spec

## Why
当前系统仅支持简单的多模型并行/串行调用（SINGLE/MULTI/JUDGE 模式），每个模型只能做同样的"审查"任务。真正的代码审查需要多角色分工——不同模型各专所长（安全审计、性能分析、代码风格等），通过 ReAct Agent 架构实现"思考-行动-观察"的智能体自主决策循环，结合 Tool 工具调用和 MCP 外部协议，达成全链路智能审查闭环。

## What Changes
- 重构 AI 审查引擎为基于 Spring AI Alibaba Agent Framework 的 ReAct 多智能体架构
- 引入**多模型角色配置**：不同 Agent 绑定不同模型，各司其职
- 每个 Agent 拥有独立的 **System Prompt**、**Tool 工具集**、**Skill 技能集**
- 引入 **Orchestrator 编排器**：负责任务分解、Agent 调度、结果汇总
- 实现 **Tool 工具层**：Git 操作、文件读取、数据库查询、代码解析等标准工具
- 引入 **Skill 技能系统**：可复用的审查能力模块（Java 规范、性能检测、安全扫描等）
- 集成 **MCP（Model Context Protocol）**：作为 Tool 的传输层，支持外部 MCP Server 扩展
- **BREAKING**: 废弃旧的 SINGLE/MULTI/JUDGE 模式，改用 Agent 编排配置
- **BREAKING**: modelsConfig 格式从简单模型名 JSON 升级为 Agent 配置 JSON

## Impact
- Affected specs: code-review-agent（已有功能增强，非破坏性替换原有流程）
- Affected code: 
  - `infrastructure/ai/` 下全部文件（重构）
  - `service/impl/ReviewServiceImpl.java`（切换执行模式）
  - `pom.xml`（确认 agent-framework 依赖，如需 MCP 需补充依赖）
  - `application.yml`（新增 Agent 线程池、MCP 配置）
  - `domain/enums/ReviewMode.java`（新增 AGENT 模式）
  - 前端 `pages/reviews/create.vue`（Agent 配置表单）

## ADDED Requirements

### Requirement: 多模型角色分配
系统 SHALL 支持为不同审查角色分配不同的大语言模型，每个角色使用最擅长该领域任务的模型。

#### Scenario: 角色-模型绑定配置
- **WHEN** 用户配置 Agent 审查时
- **THEN** 可指定每个 Agent 角色的绑定模型，例如：
  - 安全审计 Agent → 通义千问 qwen-max
  - 性能分析 Agent → DeepSeek-v3
  - 代码风格 Agent → qwen-plus
  - 架构评审 Agent → kimi-k2

#### Scenario: 角色模板预置
- **WHEN** 用户首次使用 Agent 模式
- **THEN** 系统提供预置的角色模板（安全审查员、性能分析师、代码风格检查员、异常处理专家、架构评审员），每个包含默认 System Prompt 和推荐模型

### Requirement: ReAct Agent 多智能体架构
系统 SHALL 基于 Spring AI Alibaba Agent Framework 构建 ReAct 多智能体系统，每个 Agent 独立运行"思考-行动-观察"循环。

#### Scenario: Agent 执行 ReAct 循环
- **WHEN** Agent 收到审查任务
- **THEN** 执行 ReAct 循环：
  1. **Thought**: Agent 分析任务，决定下一步行动
  2. **Action**: 调用 Tool 工具（如读取文件、查询 git log）
  3. **Observation**: 接收工具返回结果
  4. 循环直到 Agent 认为任务完成，输出 Final Answer

#### Scenario: 编排器调度多 Agent 并发
- **WHEN** 审查任务涉及多个评审维度（安全+性能+风格）
- **THEN** Orchestrator 将任务分解为子任务，分发给对应的 Agent 并发执行

#### Scenario: Agent 之间的结果传递
- **WHEN** 安全 Agent 发现潜在 SQL 注入点
- **THEN** Orchestrator 将该发现传递给架构 Agent 做进一步的上下文关联分析

### Requirement: Agent 工具系统
系统 SHALL 为 Agent 提供标准化的 Tool 工具集，Agent 通过 Function Calling 调用这些工具。

#### Scenario: Git 操作工具
- **WHEN** Agent 需要了解文件变更历史
- **THEN** 调用 `gitLogTool` 获取该文件的 commit 历史

#### Scenario: 文件读取工具
- **WHEN** Agent 需要查看变更文件的完整上下文
- **THEN** 调用 `readFileTool` 读取当前头部的完整文件内容

#### Scenario: 代码结构分析工具
- **WHEN** Agent 需要理解类依赖关系
- **THEN** 调用 `codeStructureTool` 获取指定目录下的包结构、类继承关系

#### Scenario: 结果持久化工具
- **WHEN** Agent 发现有价值的审查发现
- **THEN** 调用 `saveFindingTool` 将发现存入数据库

### Requirement: Skill 技能系统
系统 SHALL 提供可复用的 Skill 技能模块，每个 Skill 封装特定领域的审查知识和规则。

#### Scenario: 加载 Java 编码规范 Skill
- **WHEN** 审查 Java 项目时
- **THEN** Agent 加载 JavaCodeStyleSkill，其中包含阿里巴巴 Java 开发手册的规则集

#### Scenario: 加载性能检测 Skill
- **WHEN** 审查涉及数据库操作时
- **THEN** Agent 加载 PerformanceSkill，检查 N+1 查询、缺少索引、连接池配置等问题

#### Scenario: 加载安全扫描 Skill
- **WHEN** 审查涉及用户输入处理时
- **THEN** Agent 加载 SecuritySkill，检查 XSS、SQL 注入、CSRF、敏感信息泄露等

#### Scenario: Skill 动态注册
- **WHEN** 用户自定义 Skill 规则
- **THEN** 系统支持通过配置文件或数据库注册自定义 Skill，无需修改代码

### Requirement: MCP 协议集成
系统 SHALL 支持通过 MCP（Model Context Protocol）协议连接外部工具服务。

#### Scenario: 连接外部 MCP Server
- **WHEN** 配置了外部 MCP Server 地址
- **THEN** 系统通过 MCP Client 连接该 Server，自动注册其提供的 Tool 供 Agent 使用

#### Scenario: 暴露内部工具为 MCP Server
- **WHEN** 外部系统需要调用审查工具
- **THEN** 系统将内部的 Git 操作、代码分析等工具暴露为 MCP Tool，供外部 Agent 调用

#### Scenario: MCP 工具透明调用
- **WHEN** Agent 调用一个 MCP 工具
- **THEN** 调用过程对 Agent 透明，与本地 Tool 调用方式一致

### Requirement: Agent 审查流程全链路
系统 SHALL 提供端到端的 Agent 审查流程：Git diff 解析 → 任务分解 → Agent 并行执行 → 结果汇总 → 报告生成。

#### Scenario: 全链路 Agent 审查
- **WHEN** 用户触发 Agent 模式审查
- **THEN** 系统自动完成：
  1. Git diff 解析为 FileChange 列表
  2. Orchestrator 按文件/模块分解任务
  3. 各 Agent 并行执行 ReAct 审查
  4. Agent 自主调用 Tool 获取上下文
  5. 审查发现自动持久化
  6. 所有 Agent 完成后，Orchestrator 汇总生成综合报告

#### Scenario: 超大 PR 分片处理
- **WHEN** 变更文件超过 50 个
- **THEN** Orchestrator 自动将文件列表分片，每片分配给一组 Agent 并行处理

## MODIFIED Requirements

### Requirement: AI 代码审查（修改）
原 SINGLE/MULTI/JUDGE 模式**保留兼容**，新增 AGENT 模式作为推荐方式。

#### Scenario: AGENT 模式审查
- **WHEN** 用户选择 AGENT 模式并发起审查
- **THEN** 系统使用多智能体架构执行审查，Agent 自主调用工具完成深度分析
