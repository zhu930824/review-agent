# 开发环境说明

## Node.js

前端项目使用 Vite、Vue 3、TypeScript 和 `tsx --test`。

推荐使用：

- Node.js 18+
- npm

当前本机曾出现 `D:\develop\node\node.exe` 启动失败的问题，错误信息为：

```text
Could not determine Node.js install directory
```

排查结论：

- 该问题发生在 Node 运行时启动层。
- 不是前端测试断言失败。
- 后续前端验证临时使用复制到 `%TEMP%\codex-node.exe` 的 Codex bundled Node 完成。

临时验证命令：

```powershell
Copy-Item -LiteralPath 'C:\Program Files\WindowsApps\OpenAI.Codex_26.601.2237.0_x64__2p2nqsd0c76g0\app\resources\node.exe' -Destination "$env:TEMP\codex-node.exe" -Force

cd review-agent-web
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\*.test.ts
& "$env:TEMP\codex-node.exe" node_modules\vite\bin\vite.js build
```

长期建议：

- 重新安装或修复本机 `D:\develop\node`。
- 确认 `node -v`、`npm -v`、`npm test` 和 `npm run build` 可直接执行。
- 修复后优先使用 README 中的标准 npm 命令。

## 前端端口

`review-agent-web/vite.config.ts` 当前配置：

```ts
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

因此前端默认地址为：

```text
http://localhost:3000
```

## Browser E2E

Frontend browser verification uses Playwright:

```powershell
cd review-agent-web
npm run test:e2e
```

On a fresh machine, install the Chromium browser binary once before running E2E:

```powershell
cd review-agent-web
npx playwright install chromium
```

## CI Status Callback

Pre-PR status publishing is disabled by default. To post provider-neutral status payloads to an external receiver:

```powershell
$env:CI_STATUS_ENABLED='true'
$env:CI_STATUS_PROVIDER='github'
$env:CI_STATUS_API_BASE_URL='https://api.github.com'
$env:CI_STATUS_ENDPOINT='https://example.internal/review-agent/status'
$env:CI_STATUS_TOKEN='replace-me'
$env:CI_STATUS_CONTEXT='review-agent/pre-pr'
$env:CI_STATUS_TARGET_URL_TEMPLATE='https://review-agent.example/reviews/{reviewId}'
```

`CI_STATUS_PROVIDER` supports `generic`, `github`, and `gitlab`. `generic` posts to `CI_STATUS_ENDPOINT`.
For `github` and `gitlab`, `CI_STATUS_API_BASE_URL` enables dynamic commit status endpoint assembly from the project repository URL and review source commit; `CI_STATUS_ENDPOINT` remains a fallback for local receivers. GitLab project paths keep the full namespace, so multi-level groups such as `platform/tools/review-agent` are encoded correctly.

## Pre-PR Report Publishing

Pre-PR Markdown report publishing is also disabled by default. To post the backend-generated report to an internal PR comment gateway or another external receiver:

```powershell
$env:PRE_PR_REPORT_PUBLISH_ENABLED='true'
$env:PRE_PR_REPORT_PUBLISH_ENDPOINT='https://example.internal/review-agent/pre-pr-report'
$env:PRE_PR_REPORT_PUBLISH_TOKEN='replace-me'
```

Manual publishing uses `POST /api/reviews/{id}/pre-pr-report/publish`. The HTTP publisher sends JSON with `reviewId`, `format: "MARKDOWN"`, and `body`.

## 后端环境

后端需要：

- JDK 21+
- Maven 3.9+
- MySQL 8+

基础验证命令：

```powershell
cd review-agent-server
mvn -q -DskipTests compile
```
