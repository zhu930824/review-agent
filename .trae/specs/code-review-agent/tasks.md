# Tasks

- [x] Task 1: 后端项目初始化与基础设施搭建
  - [x] SubTask 1.1: 使用 Spring Initializr 创建 Spring Boot 3.x + JDK21 项目，配置 Maven 依赖（spring-ai-alibaba-starter 1.1.2、mybatis-plus、flyway、mysql-connector）
  - [x] SubTask 1.2: 配置 application.yml（数据源、Flyway、Spring AI Alibaba 的 DashScope API Key、服务端口等）
  - [x] SubTask 1.3: 建立标准四层工程结构（controller / service / domain / infrastructure），创建基础包结构
  - [x] SubTask 1.4: 配置统一的全局异常处理和统一响应体（ApiResponse）
  - [x] SubTask 1.5: 设计数据库表结构并编写 Flyway 迁移脚本（V1__init_schema.sql）

- [x] Task 2: 数据模型与持久层实现
  - [x] SubTask 2.1: 定义领域实体（Project、Review、ReviewFinding、ReviewModelResult）
  - [x] SubTask 2.2: 创建 MyBatis-Plus Mapper 接口（ProjectMapper、ReviewMapper、ReviewFindingMapper、ReviewModelResultMapper）
  - [x] SubTask 2.3: 定义 DTO 和 VO 类（请求/响应对象）

- [x] Task 3: 项目管理模块
  - [x] SubTask 3.1: 实现项目 CRUD Service（创建、查询列表、查询详情、更新、删除）
  - [x] SubTask 3.2: 实现项目 CRUD Controller REST API
  - [x] SubTask 3.3: 实现 Git 仓库异步克隆功能（项目创建后自动 clone 到本地工作目录）

- [x] Task 4: Git 代码变更解析模块
  - [x] SubTask 4.1: 封装 Git 操作工具类（clone、pull、fetch、diff）
  - [x] SubTask 4.2: 实现 diff 解析器，将 git diff 输出解析为结构化的 FileChange 对象列表（文件路径、变更类型、hunk 列表）
  - [x] SubTask 4.3: 实现分支 diff（指定分支 vs 基准分支）和 commit 范围 diff 两种模式

- [x] Task 5: AI 代码审查引擎
  - [x] SubTask 5.1: 配置 Spring AI Alibaba ChatClient，支持通义千问等模型的调用
  - [x] SubTask 5.2: 设计 AI 审查的 Prompt 模板（系统提示词 + 审查维度：代码规范、潜在 Bug、性能问题、安全漏洞、异常处理）
  - [x] SubTask 5.3: 实现单文件审查逻辑（将代码 diff 片段 + prompt 发送给 AI，解析返回的审查意见）
  - [x] SubTask 5.4: 实现批量审查调度（将多文件变更分批、并行发送给 AI，汇总结果）

- [x] Task 6: 多模型交叉审查与 Judge 机制
  - [x] SubTask 6.1: 支持多模型配置（可指定多个模型名称及 API Key）
  - [x] SubTask 6.2: 实现多模型并行审查（多个模型同时审查同一份代码，收集各自结果）
  - [x] SubTask 6.3: 实现交叉验证汇总（标记多模型共同发现的问题为高置信度）
  - [x] SubTask 6.4: 实现 Judge 模型评审机制（Worker 模型产出后，由 Judge 模型二次评估审查质量）

- [x] Task 7: Pre-PR 预审与审查报告
  - [x] SubTask 7.1: 实现 Pre-PR 检查接口（执行审查，按阻塞级别判定通过/阻塞）
  - [x] SubTask 7.2: 实现审查报告生成（摘要统计、问题分类汇总、按文件分组、置信度标记）
  - [x] SubTask 7.3: 实现审查记录查询接口（按项目、时间、状态筛选，支持分页）

- [x] Task 8: 前端项目初始化与布局框架
  - [x] SubTask 8.1: 使用 Nuxt 3 初始化前端项目，配置 TypeScript、Nuxt UI 组件库
  - [x] SubTask 8.2: 搭建全局布局（侧边导航 + 顶栏 + 内容区），配置路由结构
  - [x] SubTask 8.3: 封装 API 请求工具（基于 $fetch 或 useFetch，统一处理错误和响应）

- [x] Task 9: 前端项目管理页面
  - [x] SubTask 9.1: 实现项目列表页（卡片或表格展示，支持搜索）
  - [x] SubTask 9.2: 实现项目创建/编辑表单页（表单校验）
  - [x] SubTask 9.3: 实现项目详情页（基本信息、最近审查记录列表）

- [x] Task 10: 前端审查触发与报告展示
  - [x] SubTask 10.1: 实现审查触发页（选择项目、分支/commit 范围、模型配置，发起审查）
  - [x] SubTask 10.2: 实现代码差异查看组件（side-by-side diff 视图，语法高亮）
  - [x] SubTask 10.3: 实现审查报告详情页（审查摘要、问题列表、筛选排序、多模型结果对比）
  - [x] SubTask 10.4: 实现仪表盘页（审查统计卡片、趋势图、最近审查列表）

# Task Dependencies
- Task 2 依赖 Task 1（需要先有项目结构和数据库）
- Task 3 依赖 Task 2（需要先有数据模型和 Mapper）
- Task 4 可与 Task 3 并行
- Task 5 依赖 Task 4（需要先有 diff 数据）
- Task 6 依赖 Task 5（在单模型基础上扩展多模型）
- Task 7 依赖 Task 5、Task 6（Pre-PR 和报告依赖审查引擎）
- Task 8 可与 Task 1-7 并行（前后端独立开发）
- Task 9 依赖 Task 3、Task 8（需要后端 API 和前端框架）
- Task 10 依赖 Task 7、Task 8、Task 9（需要完整后端审查能力和前端框架）
