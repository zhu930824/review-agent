package com.review.agent.infrastructure.agent.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPipelineConfig {
    /** 本次审查启用的 Agent 列表 */
    private List<AgentConfig> agents;
    /** 编排策略：PARALLEL(并行) / SEQUENTIAL(串行) / HYBRID(混合) */
    private String orchestrationStrategy;
    /** 是否启用 MCP 外部工具 */
    private boolean mcpEnabled;
    /** MCP Server 地址列表 */
    private List<String> mcpServerUrls;
}
