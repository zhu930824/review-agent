# Checklist

## Agent 基础设施
- [x] ReviewMode.AGENT 枚举值已添加，数据库 review_mode 列支持 'AGENT' 值
- [x] agentTaskExecutor 线程池配置正确（核心10/最大20/队列50）
- [x] AgentConfig 模型包含角色名、模型名、System Prompt、Skill 列表、temperature
- [x] Agent 配置 JSON Schema 定义清晰，前后端一致

## Tool 工具层
- [x] GitLogTool 可正确查询指定文件的 commit 历史
- [x] ReadFileTool 可读取 HEAD 版本文件的完整内容
- [x] CodeStructureTool 可解析 Java 项目的包结构和类依赖
- [x] SaveFindingTool 可正确将 finding 持久化到 review_finding 表
- [x] 所有 Tool 使用 `@Tool` 注解注册，Agent 可通过 Function Calling 调用

## Skill 技能系统
- [x] Skill 接口定义完整（name、description、knowledgeRules、enabled）
- [x] JavaCodeStyleSkill 包含主要的阿里巴巴 Java 规范规则
- [x] PerformanceSkill 包含 N+1 查询、资源泄漏等检测规则
- [x] SecuritySkill 包含 XSS、SQL 注入、CSRF 等安全规则
- [x] ExceptionHandlingSkill 包含异常捕获、吞没等检测规则
- [x] SkillRegistry 支持配置文件 + 注解双重加载

## ReAct Agent 核心
- [x] ReviewAgent 可成功绑定模型、工具集、技能、System Prompt
- [x] AgentFactory 根据 AgentConfig 创建完整配置的 Agent 实例
- [x] AgentExecutionResult 和 ReviewOrchestrator 正确执行 ReAct 流程
- [x] 5 种预设角色模板（安全审计员、性能分析师、代码风格检查员、异常处理专家、架构评审员）均已定义
- [x] Agent 执行日志完整记录每个步骤的 prompt 和响应（通过 createModelResult/completeModelResult）

## 编排器 Orchestrator
- [x] ReviewOrchestrator 可正确分解任务并按文件/模块分发
- [x] 多 Agent 并发执行（CompletableFuture），等待全部完成后汇总
- [x] 结果汇总能去重合并，按文件分组，计算综合置信度
- [x] 超大 PR（>50文件）自动分片处理（每批最多30文件）

## MCP 协议集成
- [x] spring-ai-alibaba-agent-framework 传递依赖已包含 MCP SDK
- [x] MCP Client 可成功连接外部 MCP Server（预留实现，条件启用）
- [x] 外部 MCP Server 的工具自动注册到 Agent 可用工具集（MCPToolBridge）
- [x] 内部工具已暴露为 MCP Server（预留实现，条件启用）
- [x] MCP 相关配置项在 application.yml 中完整

## AGENT 模式审查流程
- [x] executeReview 中 AGENT 模式分支正常工作
- [x] Agent 执行步骤日志正确记录到 review_model_result 表（Orchestrator 管理）
- [x] Agent 审查超时控制生效（单 Agent 最长 5 分钟，orTimeout）
- [x] 原 SINGLE/MULTI/JUDGE 模式仍然正常可用
- [x] AGENT 模式审查报告包含所有 Agent 的发现和步骤摘要

## 前端 Agent 配置界面
- [x] 发起审查页新增 AGENT 模式选项
- [x] Agent 角色配置表单可正常选择角色、绑定模型、勾选 Skill
- [x] 审查详情页可展示 Agent 执行步骤日志和多 Agent 对比结果
- [x] 前后端 Agent 配置格式一致，提交和回显正确（AgentPipelineConfig JSON）
