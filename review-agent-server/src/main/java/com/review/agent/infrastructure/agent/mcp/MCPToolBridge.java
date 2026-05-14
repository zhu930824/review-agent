package com.review.agent.infrastructure.agent.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 工具桥接器：将外部 MCP Tool 和内部本地 Tool 合并为统一的工具列表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MCPToolBridge {

    private final List<ToolCallback> localTools;

    /**
     * 获取 Agent 可用的完整工具列表（本地 + MCP 远程）
     * MCP 远程工具通过 mcpToolCallbacks Bean 自动注入到 localTools 中
     */
    public List<ToolCallback> getAvailableTools() {
        List<ToolCallback> allTools = new ArrayList<>(localTools);
        log.debug("当前可用工具数量: {}", allTools.size());
        return allTools;
    }
}
