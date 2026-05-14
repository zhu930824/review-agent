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
public class AgentConfig {
    /** Agent 角色名称 */
    private String role;
    /** 绑定的模型名称 */
    private String modelName;
    /** 系统提示词 */
    private String systemPrompt;
    /** 启用的技能列表 */
    private List<String> skills;
    /** 温度参数 (0.0-2.0) */
    private Double temperature;
    /** 最大 ReAct 循环步数 */
    private Integer maxSteps;
}
