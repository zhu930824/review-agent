# Review Agent

Review Agent 是一个面向 AI Code Review 和 Pre-PR 质量门禁的研发治理平台。它把项目管理、模型策略、代码审查、人工确认、治理能力目录和运营指标组织到同一个工作流里，帮助团队在 AI Coding 提速后继续控制质量、风险和技术债。

## 功能概览

- 项目管理：登记 Git 仓库、维护默认分支、查看克隆状态和失败信息。
- 发起审查：支持常规 Review 和 Pre-PR 审查流程。
- 多模型策略：管理模型供应商、模型档案、审查策略和角色绑定。
- 质量门禁：围绕 BLOCKER、MAJOR、人工确认和跨模型命中组织 Pre-PR 准入。
- 审查详情：展示模型执行状态、发现项、阻断原因、人工确认和智能分析。
- 治理中心：展示市场能力对标、治理成熟度、策略包和集成路线图。
- 运营中心：汇总待处理风险、规则学习候选、SLA 压力和业务影响。
- 登录注册：后端使用 JWT 鉴权，前端统一通过认证存储管理登录态。

## 技术栈

### 后端

- Java 21
- Spring Boot 3.3.5
- Spring AI Alibaba / DashScope
- MyBatis Plus
- MySQL 8+
- Flyway
- JGit
- JWT / BCrypt

### 前端

- Vite
- Vue 3
- TypeScript
- Vue Router
- Ant Design Vue
- `tsx --test` + Node test runner

## 目录结构

```text
review-agent/
├── review-agent-server/          # Spring Boot 后端
│   ├── src/main/java/com/review/agent/
│   │   ├── controller/            # REST API
│   │   ├── domain/                # DTO、实体、枚举
│   │   ├── infrastructure/        # Git、AI、工作流、治理、持久化
│   │   └── service/               # 应用服务
│   └── src/main/resources/
│       ├── application.yml        # 后端配置
│       └── db/migration/          # Flyway 数据库迁移
├── review-agent-web/              # Vite + Vue 前端
│   ├── src/components/            # 页面组件
│   ├── src/composables/           # API、认证等组合函数
│   ├── src/router/                # Vue Router
│   ├── src/views/                 # 路由页面
│   ├── src/utils/                 # 纯函数和业务 helper
│   └── tests/                     # 前端业务 helper 测试
├── doc/                           # 业务文章、SOP、设计素材
└── docs/                          # 规格和执行计划
```

## 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+ 或 20+
- npm
- MySQL 8+

## 本地启动

### 1. 准备数据库

```sql
CREATE DATABASE review_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端启动时会通过 Flyway 自动执行 `review-agent-server/src/main/resources/db/migration` 下的迁移脚本。

### 2. 配置环境变量

常用环境变量：

```bash
DASHSCOPE_API_KEY=your-dashscope-api-key
REPO_BASE_PATH=./repos
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRE_HOURS=168
```

默认数据库配置在 `review-agent-server/src/main/resources/application.yml`。如果本地数据库账号不同，请通过配置文件或环境变量覆盖。

### 3. 启动后端

```bash
cd review-agent-server
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 4. 启动前端

```bash
cd review-agent-web
npm install
npm run dev
```

默认地址：

```text
http://localhost:3000
```

前端请求统一走 `/api`。本地开发时通常由 Vite 代理到后端服务。

## 登录与鉴权

- 注册接口：`POST /api/auth/register`
- 登录接口：`POST /api/auth/login`
- 退出接口：`POST /api/auth/logout`
- 业务接口默认要求 `Authorization: Bearer <token>`
- 前端 token 存储 key：`review-agent-token`
- 前端用户信息存储 key：`review-agent-user`
- SSE 进度接口 `/api/reviews/{id}/progress` 当前保持放行，避免原生 `EventSource` 无法携带 Authorization header 的问题。

首次访问业务页面时，如果未登录会跳转到 `/login`。

## 核心页面

- `/login`：登录页
- `/register`：注册页
- `/`：质量驾驶舱
- `/projects`：项目管理
- `/reviews/create`：发起 Pre-PR 审查
- `/reviews/:id`：审查详情
- `/governance`：治理中心
- `/operations`：运营中心
- `/settings/models`：模型配置
- `/knowledge`：知识中心
- `/gateway`：AI 网关

## 常用命令

### 后端编译

```bash
cd review-agent-server
mvn -q -DskipTests compile
```

### 前端测试

```bash
cd review-agent-web
npm test
```

### 前端生产构建

```bash
cd review-agent-web
npm run build
```

## 开发说明

- 数据库结构变更通过 Flyway 新增迁移脚本，不直接修改历史迁移。
- 前端请求统一走 `useApi`，会自动附加 JWT。
- 登录态统一通过 `useAuth` 和 `src/utils/authStorage.ts` 管理。
- 业务页面使用 `src/views/layouts/DefaultLayout.vue`。
- 后端统一响应体为 `Result<T>`。
- 部分 Java 文件在普通文本读取时可能出现 `Esafenet` 保护内容；修改核心后端编排代码前，应先确认源码资产可维护性。
- API 清单见 `docs/api-catalog.md`。
- 开发环境补充说明见 `docs/development-environment.md`。

## 验证清单

提交前建议至少执行：

```bash
cd review-agent-server
mvn -q -DskipTests compile

cd ../review-agent-web
npm test
npm run build
```

如果改动涉及登录态，还应手动验证：

- 未登录访问业务页会跳转 `/login`
- 注册成功后进入首页
- 登录成功后刷新仍保持登录
- 退出登录后回到 `/login`
- 未携带 token 访问业务接口返回 401
