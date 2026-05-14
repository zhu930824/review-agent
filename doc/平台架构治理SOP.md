# Review Agent 平台架构治理 SOP

## 目标

让 Review Agent 从一次性的 AI 代码审查工具，升级为能支撑业务交付的 AI Coding 治理平台。平台要前置发现技术债、降低人工 CR 压力，并把团队工程共识固化为模型策略、Agent 角色和 Pre-PR 质量闸门。

## 当前约束

- 根目录不是 Git 仓库，本次无法按常规方式提交设计和计划文档。
- Maven 当前读取 `JAVA_HOME=D:\develop\JDK11`，而后端 `pom.xml` 目标版本是 Java 21。后端编译前需要将 Maven 的 `JAVA_HOME` 指向完整 JDK 21。
- 部分 Java 源文件在普通文本读取时出现 `Esafenet` 保护内容。本轮不覆盖既有 Java 源文件，只新增 SQL、文档和前端可验证代码。

## 分层目标

- Application：审查创建、Pre-PR 闸门、策略编译、人工决策等用例编排。
- Domain：Review、Finding、ModelProvider、ModelProfile、ReviewStrategy、PrePrGate 等业务对象。
- Infrastructure：MyBatis、Git、模型客户端、MCP bridge、外部工具适配。
- Common：统一响应、异常、校验、分页和通用工具。

## Pre-PR 流程

1. RD 选择项目、源分支、目标分支和审查策略。
2. 平台将策略编译为后端兼容的 `modelsConfig`。
3. 多模型或多 Agent 并行审查代码 diff。
4. 平台按严重度、交叉命中和 Judge 结果生成质量结论。
5. BLOCKER 默认阻断；MAJOR 默认进入人工复核；MINOR 和 INFO 作为建议关注项。
6. 人工确认或忽略发现项，最终形成 PR 摘要和 Pre-PR 结论。

## 多模型配置落地顺序

1. 前端内置模型供应商、模型档案和审查策略，先消除手写 JSON。
2. 使用 `review.models_config` 保持后端兼容。
3. 增加 `V2__model_strategy_governance.sql`，为后端持久化准备表结构。
4. 后端源码可安全编辑后，新增模型配置 API 和策略编译 Application Service。
5. 将前端内置种子数据迁移为后端返回数据。

## 质量保障

- 新增前端纯函数必须先写测试，再实现。
- 每次前端页面改动后运行 `npm test` 和 `npm run build`。
- 后端源码改动前必须先解决 Maven JDK 版本，并确认源码可正常编译。
- 数据库迁移只做新增表或新增索引，不破坏现有 `review`、`review_finding`、`review_model_result` 的兼容性。

## 后续后端接入建议

优先新增以下 API：

- `GET /model-providers`
- `GET /model-profiles`
- `GET /review-strategies`
- `POST /reviews/pre-pr`
- `PATCH /reviews/{id}/pre-pr-decision`

第一版 API 可以只读返回种子数据，第二版再支持管理端编辑和审计。
