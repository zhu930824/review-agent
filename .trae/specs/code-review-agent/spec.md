# Code Review Agent 服务规格说明

## Why
在 AI 辅助编码占比超过 90% 的团队中，Code Review 成为全链路瓶颈——AI 压缩了编码时间，压力系统性地向下游 CR 环节集中。需要构建一个 AI 驱动的代码审查 Agent 服务，实现"AI 审查规范类问题 + 多模型交叉验证 + 人工聚焦业务语义"的分层 CR 机制，将人工 Reviewer 的认知负担从"你写得对吗"转变到"我们是否在正确的约束下解决正确的问题"。

## What Changes
- 新建完整的 Code Review Agent 后端服务（Spring AI Alibaba + JDK21 + MySQL）
- 新建前端管理界面（Vue3 + Nuxt）
- 支持 Git 仓库集成，自动解析代码变更（diff）
- 支持多 AI 模型并行审查与交叉验证
- 支持 Pre-PR 预审机制：提交前 AI 自查
- 支持审查报告自动生成（结构化问题分类：规范、Bug、性能、安全、异常处理）
- 支持审查结果持久化存储与历史追溯
- 支持项目/仓库管理、审查规则配置

## Impact
- Affected specs: 无（全新项目）
- Affected code: 全新项目，无现有代码影响

## ADDED Requirements

### Requirement: 项目管理
系统 SHALL 提供项目（仓库）的创建与管理能力，作为代码审查的基本组织单元。

#### Scenario: 创建项目
- **WHEN** 用户填写项目名称、Git 仓库地址、默认分支等信息并提交
- **THEN** 系统创建项目记录，自动初始化 Git 仓库克隆（异步），状态流转为"就绪"

#### Scenario: 查看项目列表
- **WHEN** 用户访问项目列表页
- **THEN** 系统展示所有项目，包含名称、仓库地址、最近审查时间、审查次数等信息

### Requirement: 代码变更解析
系统 SHALL 能够解析 Git 仓库的代码变更（diff），提取变更文件、变更内容和上下文信息。

#### Scenario: 解析指定分支的 diff
- **WHEN** 用户触发对某项目某分支的审查
- **THEN** 系统拉取最新代码，获取与基准分支（默认 main/master）的 diff，解析为结构化的文件变更列表

#### Scenario: 解析指定 commit 范围的 diff
- **WHEN** 用户指定两个 commit SHA 发起审查
- **THEN** 系统获取这两个 commit 之间的 diff，解析为结构化的文件变更列表

### Requirement: AI 代码审查
系统 SHALL 使用 Spring AI Alibaba 调用大语言模型对代码变更进行多维度审查。

#### Scenario: 单模型审查
- **WHEN** 用户选择单个 AI 模型发起审查
- **THEN** 系统将 diff 内容按文件分批发送给指定模型，模型返回审查意见（问题分类、严重级别、建议修复方案）

#### Scenario: 多模型交叉审查
- **WHEN** 用户配置多个 AI 模型（如通义千问 + DeepSeek）并发起审查
- **THEN** 系统并行调用多个模型对同一份代码变更进行审查，汇总各模型结果并标记交叉命中的问题为高置信度

#### Scenario: 高阶模型评审低阶模型产出
- **WHEN** 用户配置一个 Judge 模型和多个 Worker 模型
- **THEN** 系统先由 Worker 模型产出审查意见，再由 Judge 模型对 Worker 模型的审查质量进行二次评估，生成综合报告

### Requirement: Pre-PR 预审机制
系统 SHALL 支持在代码提交前进行 AI 自查，过滤基础规范类问题。

#### Scenario: Pre-PR 检查通过
- **WHEN** 开发者在提交 PR 前触发 Pre-PR 检查
- **THEN** 系统执行 AI 审查，若无严重问题则返回"通过"状态

#### Scenario: Pre-PR 检查发现阻塞性问题
- **WHEN** AI 审查发现代码存在阻塞级别的问题（如安全漏洞、空指针风险）
- **THEN** 系统返回"阻塞"状态，列出所有必须修复的问题，并要求修复后重新检查

### Requirement: 审查报告生成
系统 SHALL 将 AI 审查结果生成结构化的审查报告。

#### Scenario: 生成审查报告
- **WHEN** 一轮审查完成
- **THEN** 系统生成报告，包含：
  - 审查摘要（总问题数、各严重级别分布）
  - 按文件分组的问题列表
  - 每个问题的详情（类别、严重级别、代码位置、问题描述、建议修复）
  - 多模型交叉验证的置信度标记
  - 人工 Review 状态标记

### Requirement: 审查结果持久化
系统 SHALL 将每次审查的结果持久化到 MySQL 数据库。

#### Scenario: 存储审查记录
- **WHEN** 审查完成
- **THEN** 系统将审查记录（项目、分支、commit 范围、模型配置、审查结果）存入数据库，支持历史查询

#### Scenario: 查询历史审查
- **WHEN** 用户按项目、时间范围、状态等条件查询审查历史
- **THEN** 系统返回匹配的审查记录列表

### Requirement: 前端管理界面
系统 SHALL 提供基于 Vue3 + Nuxt 的前端管理界面。

#### Scenario: 仪表盘
- **WHEN** 用户访问首页
- **THEN** 展示审查统计数据（总审查次数、问题总数、各严重级别趋势图）、最近审查列表

#### Scenario: 代码差异查看
- **WHEN** 用户点击某次审查的某文件
- **THEN** 展示该文件的 diff 视图（side-by-side 或 unified），并高亮标注 AI 发现的问题位置

#### Scenario: 审查报告详情
- **WHEN** 用户查看某次审查详情
- **THEN** 展示完整审查报告，包括问题列表、多模型交叉验证结果、支持筛选和排序

### Requirement: 数据库版本管理
系统 SHALL 使用 Flyway 管理数据库 schema 的版本迁移。

#### Scenario: 首次部署
- **WHEN** 系统首次启动
- **THEN** Flyway 自动执行所有迁移脚本，创建完整的数据库表结构

#### Scenario: 版本升级
- **WHEN** 系统版本升级，包含新的迁移脚本
- **THEN** Flyway 自动执行增量迁移脚本，更新数据库 schema
