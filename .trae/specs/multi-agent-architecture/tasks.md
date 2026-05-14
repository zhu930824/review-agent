# Tasks

- [x] Task 1: Agent 基础设施搭建
  - [x] SubTask 1.1: 新增 `ReviewMode.AGENT` 枚举值，数据库 review_mode 字段支持 AGENT
  - [x] SubTask 1.2: 创建 Agent 线程池配置（agentTaskExecutor，核心10/最大20/队列50）
  - [x] SubTask 1.3: 设计 Agent 配置模型（AgentConfig：角色名、绑定模型名、System Prompt、Skill 列表、temperature 等）
  - [x] SubTask 1.4: 设计 Agent 配置 JSON Schema（用于前端表单和入库存储）

- [x] Task 2: Tool 工具层实现
  - [x] SubTask 2.1: 创建 `infrastructure/agent/tool/` 包，定义工具接口与注册机制
  - [x] SubTask 2.2: 实现 `GitLogTool`：查询指定文件的 commit 历史和变更作者
  - [x] SubTask 2.3: 实现 `ReadFileTool`：读取当前 HEAD 版本指定文件的完整内容
  - [x] SubTask 2.4: 实现 `CodeStructureTool`：解析 Java 项目包结构和类依赖关系
  - [x] SubTask 2.5: 实现 `SaveFindingTool`：将 Agent 发现的审查问题持久化到数据库
  - [x] SubTask 2.6: 使用 Spring AI 的 `@Tool` 注解注册工具，确保可被 Agent 通过 Function Calling 调用

- [x] Task 3: Skill 技能系统实现
  - [x] SubTask 3.1: 创建 `infrastructure/agent/skill/` 包，定义 Skill 接口（name、description、knowledgeRules、enabled）
  - [x] SubTask 3.2: 实现 `JavaCodeStyleSkill`：加载阿里巴巴 Java 开发手册规则（命名、格式、注释）
  - [x] SubTask 3.3: 实现 `PerformanceSkill`：数据库 N+1 查询检测、资源泄漏、缓存策略等规则
  - [x] SubTask 3.4: 实现 `SecuritySkill`：XSS、SQL 注入、CSRF、敏感信息泄露、权限校验规则
  - [x] SubTask 3.5: 实现 `ExceptionHandlingSkill`：异常捕获、异常吞没、异常信息完整性规则
  - [x] SubTask 3.6: 实现 Skill 注册器 `SkillRegistry`，支持配置文件 + 注解双重加载方式

- [x] Task 4: ReAct Agent 核心实现
  - [x] SubTask 4.1: 创建 `infrastructure/agent/ReviewAgent.java`：封装 Spring AI Alibaba Agent，绑定模型+工具+技能+System Prompt
  - [x] SubTask 4.2: 实现 Agent 工厂 `AgentFactory`：根据 AgentConfig 创建配置完整的 ReviewAgent 实例
  - [x] SubTask 4.3: 创建 `AgentExecutionResult` 和 `AgentRunner` 相关类
  - [x] SubTask 4.4: 为每种预设角色创建专用 Agent 配置模板（安全审计员、性能分析师、代码风格检查员、异常处理专家、架构评审员）

- [x] Task 5: 编排器 Orchestrator 实现
  - [x] SubTask 5.1: 创建 `ReviewOrchestrator.java`：任务分解、Agent 调度、结果汇总
  - [x] SubTask 5.2: 实现任务分解策略：按文件类型/模块/审查维度拆分
  - [x] SubTask 5.3: 实现 Agent 并发调度：多 Agent 并行执行，等待全部完成后汇总
  - [x] SubTask 5.4: 实现结果汇总：合并去重所有 Agent 的 finding，按文件分组，计算综合置信度
  - [x] SubTask 5.5: 实现超大 PR 分片处理（变更文件 > 50 时分批，每批不超过 30 个文件）

- [x] Task 6: MCP 协议集成
  - [x] SubTask 6.1: 确认 pom.xml 中已包含 MCP 相关依赖（agent-framework 传递依赖已包含 MCP SDK）
  - [x] SubTask 6.2: 创建 `MCPClientConfig.java`：配置 MCP Client 连接外部 MCP Server
  - [x] SubTask 6.3: 创建 `MCPServerConfig.java`：将内部 Tool 暴露为 MCP Tool，供外部 Agent 调用
  - [x] SubTask 6.4: 实现 MCP Tool 自动注册：外部 MCP Server 的工具自动纳入 Agent 可用工具集
  - [x] SubTask 6.5: 添加 MCP 相关 application.yml 配置项

- [x] Task 7: Agent 审查流程编排（AGENT 模式）
  - [x] SubTask 7.1: 创建 `AgentReviewService.java`：AGENT 模式审查的完整编排服务
  - [x] SubTask 7.2: 修改 `ReviewServiceImpl.executeReview()`，增加 AGENT 模式分支
  - [x] SubTask 7.3: 实现 Agent 执行的步骤追踪：记录每个 Agent 的 ReAct 步骤日志
  - [x] SubTask 7.4: 实现 Agent 审查超时控制（单个 Agent 最长执行 5 分钟）
  - [x] SubTask 7.5: 保留原有 SINGLE/MULTI/JUDGE 模式兼容，通过 ReviewMode 区分

- [x] Task 8: 前后端 Agent 配置界面
  - [x] SubTask 8.1: 更新前端类型定义（review.ts），新增 Agent 配置相关类型
  - [x] SubTask 8.2: 更新 `pages/reviews/create.vue`：审查模式新增 AGENT 选项
  - [x] SubTask 8.3: 实现 Agent 角色配置表单：角色选择、模型绑定、Skill 勾选
  - [x] SubTask 8.4: 更新 `pages/reviews/[id].vue`：展示 Agent 执行步骤日志和多 Agent 对比结果

# Task Dependencies
- Task 2 依赖 Task 1（需要先有 Agent 基础设施和配置模型）
- Task 3 可与 Task 2 并行
- Task 4 依赖 Task 1、Task 2、Task 3（Agent 需要配置+工具+技能）
- Task 5 依赖 Task 4（编排器依赖 Agent 实例）
- Task 6 可与 Task 2、Task 3、Task 4 并行（MCP 是独立协议层）
- Task 7 依赖 Task 4、Task 5、Task 6（完整流程需要所有组件）
- Task 8 依赖 Task 7（前端需要后端 API 就绪）
