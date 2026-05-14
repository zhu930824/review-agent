package com.review.agent.infrastructure.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Agent 工具注册配置：将 @Tool 注解的 Bean 转换为统一的 ToolCallback 列表，
 * 供 MCPToolBridge 注入后合并到 Agent 可用工具集中。
 */
@Slf4j
@Configuration
public class AgentToolConfig {

    @Bean
    public List<ToolCallback> localTools(
            GitLogTool gitLogTool,
            ReadFileTool readFileTool,
            CodeStructureTool codeStructureTool,
            SaveFindingTool saveFindingTool) {
        ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(gitLogTool, readFileTool, codeStructureTool, saveFindingTool)
                .build();
        List<ToolCallback> callbacks = Arrays.asList(provider.getToolCallbacks());
        log.info("已注册本地工具: {} 个", callbacks.size());
        return callbacks;
    }
}
