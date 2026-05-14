package com.review.agent.infrastructure.agent;

import com.review.agent.infrastructure.agent.config.AgentConfig;
import com.review.agent.infrastructure.agent.mcp.MCPToolBridge;
import com.review.agent.infrastructure.agent.skill.SkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final ChatModel chatModel;
    private final SkillRegistry skillRegistry;
    private final MCPToolBridge mcpToolBridge;

    /** 根据 AgentConfig 创建基于 ReactAgent 的 ReviewAgent 实例，MemorySaver 由 Agent 内部创建 */
    public ReviewAgent createAgent(AgentConfig config) {
        List<ToolCallback> tools = mcpToolBridge.getAvailableTools();
        return new ReviewAgent(config, chatModel, tools, skillRegistry);
    }
}
