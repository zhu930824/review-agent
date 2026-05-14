# Review Agent

Review Agent 是一个面向 AI Code Review 和 Pre-PR 质量门禁的研发治理平台。它提供项目管理、代码审查、模型配置、治理能力对标、运营看板，以及基于 JWT 的登录注册能力。

## 功能概览

- 项目管理：登记 Git 仓库、维护默认分支、查看克隆状态和克隆失败信息。
- 发起审查：支持常规 Review 和 Pre-PR 审查流程。
- 多模型策略：管理模型供应商、模型档案、审查策略和角色绑定。
- 质量门禁：围绕 blocker、major、人工确认等信号组织 Pre-PR 准入。
- 治理中心：展示市场能力对标、治理成熟度、策略包和集成路线图。
- 登录注册：后端使用 JWT 鉴权，前端使用登录态守卫保护业务页面。

## 技术栈

### 后端

- Java 21
- Spring Boot 3.3.5
- Spring AI Alibaba / DashScope
- MyBatis Plus
- MySQL
- Flyway
- JGit
- BCrypt 密码哈希

### 前端

- Nuxt 3
- Vue 3
- TypeScript
- Nuxt UI
- Tailwind CSS

## 目录结构

```text
review-agent/
├── review-agent-server/          # Spring Boot 后端
│   ├── src/main/java/com/review/agent/
│   │   ├── controller/            # REST API
│   │   ├── domain/                # DTO、实体、异常
│   │   ├── infrastructure/        # 配置、鉴权、Git、持久化
│   │   └── service/               # 应用服务
│   └── src/main/resources/
│       ├── application.yml        # 后端配置
│       └── db/migration/          # Flyway 数据库迁移
├── review-agent-web/             # Nuxt 前端
│   ├── components/                # 页面组件
│   ├── composables/               # API、认证等组合函数
│   ├── layouts/                   # default/auth 布局
│   ├── middleware/                # 路由守卫
│   ├── pages/                     # 路由页面
│   ├── tests/                     # 前端业务 helper 测试
│   └── utils/                     # 纯函数和内置策略
├── doc/                           # 设计和 SOP 文档
└── docs/                          # 规划文档
```

## 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+ 或 20+
- npm
- MySQL 8+

## 本地启动

### 1. 准备数据库

创建数据库：

```sql
CREATE DATABASE review_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端启动时会通过 Flyway 自动执行 `review-agent-server/src/main/resources/db/migration` 下的迁移脚本。

### 2. 配置环境变量

后端默认读取 `application.yml`，常用环境变量如下：

```bash
DASHSCOPE_API_KEY=your-dashscope-api-key
REPO_BASE_PATH=./repos
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRE_HOURS=168
```

默认后端数据库配置在 `review-agent-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/review_agent
    username: root
    password: 520824
```

如本地数据库账号不同，请先调整配置或通过环境覆盖。

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

前端默认 API 地址在 `review-agent-web/nuxt.config.ts` 中配置：

```ts
apiBaseUrl: process.env.API_BASE_URL || 'http://localhost:8080/api'
```

如果后端地址不同，可设置：

```bash
API_BASE_URL=http://localhost:8080/api
```

## 登录与鉴权

- 注册接口：`POST /api/auth/register`
- 登录接口：`POST /api/auth/login`
- 退出接口：`POST /api/auth/logout`
- 业务接口默认要求 `Authorization: Bearer <token>`
- 前端将 JWT 保存在 `localStorage`
- 退出登录时前端删除 token，后端不维护 token 黑名单
- SSE 进度接口 `/api/reviews/{id}/progress` 当前保持放行，避免原生 `EventSource` 无法携带 Authorization header

首次访问业务页面时，如果未登录会自动跳转到 `/login`。

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

## 核心页面

- `/login`：登录页
- `/register`：注册页
- `/`：质量驾驶舱
- `/projects`：项目管理
- `/reviews/create`：发起审查
- `/governance`：治理中心
- `/operations`：运营中心
- `/settings/models`：模型配置

## 开发说明

- 数据库结构变更通过 Flyway 新增迁移脚本，不直接修改历史迁移。
- 前端请求统一走 `useApi`，会自动附加 JWT。
- 登录态统一由 `useAuth` 管理。
- 业务页面默认使用 `layouts/default.vue`，登录注册页使用 `layouts/auth.vue`。
- 后端统一响应体为 `ApiResponse<T>`。

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

- 未登录访问首页会跳转 `/login`
- 注册成功后进入首页
- 登录成功后刷新仍保持登录
- 退出登录后回到 `/login`
- 未携带 token 访问业务接口返回 401

