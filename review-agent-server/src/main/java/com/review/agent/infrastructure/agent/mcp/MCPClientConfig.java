package com.review.agent.infrastructure.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MCP Client 配置，仅在 review-agent.mcp.enabled=true 时启用
 * 用于连接外部 MCP Server，将其工具注册到 Agent 可用工具集中
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "review-agent.mcp.enabled", havingValue = "true")
public class MCPClientConfig {

    /**
     * 从配置中读取 MCP Server URL 列表，注册为 ToolCallback
     * 具体实现依赖 MCP SDK 的 McpSyncClient
     */
    @Bean
    public List<ToolCallback> mcpToolCallbacks() {
        // 预留实现：未来通过 McpSyncClient.using(transport).sync() 连接外部 MCP Server
        log.info("MCP Client 已启用，准备连接外部 MCP Server");
        return List.of();
    }
}
