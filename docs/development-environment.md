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
