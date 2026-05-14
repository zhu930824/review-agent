package com.review.agent.infrastructure.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 配置，将内部 Tool 暴露为 MCP Tool 供外部 Agent 调用
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "review-agent.mcp.server.enabled", havingValue = "true")
public class MCPServerConfig {

    @Bean
    public Object mcpServer() {
        // 预留实现：将内部 Tool 注册为 MCP Server Tool
        // 具体 API 依赖 MCP SDK 的 McpSyncServer
        log.info("MCP Server 已启用，内部工具将暴露为 MCP Tool");
        return new Object();
    }
}
