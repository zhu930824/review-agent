package com.review.agent.infrastructure.agent;

import com.review.agent.infrastructure.agent.config.AgentConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 5 种预设角色模板：安全审计员、性能分析师、代码风格检查员、异常处理专家、架构评审员
 */
public final class AgentRoleTemplates {

    private AgentRoleTemplates() {
    }

    public static Map<String, AgentConfig> getPresets() {
        Map<String, AgentConfig> presets = new LinkedHashMap<>();

        presets.put("SECURITY_AUDITOR", AgentConfig.builder()
                .role("安全审计员")
                .modelName("qwen-max")
                .systemPrompt("你是资深安全审计专家。审查代码的安全漏洞，包括SQL注入、XSS、CSRF、敏感信息泄露、权限绕过等问题。")
                .skills(List.of("SecuritySkill"))
                .temperature(0.2)
                .maxSteps(10)
                .build());

        presets.put("PERFORMANCE_ANALYST", AgentConfig.builder()
                .role("性能分析师")
                .modelName("qwen-plus")
                .systemPrompt("你是资深性能分析专家。审查代码的性能问题，包括N+1查询、资源泄漏、低效算法、缓存策略等。")
                .skills(List.of("PerformanceSkill"))
                .temperature(0.3)
                .maxSteps(10)
                .build());

        presets.put("CODE_STYLE_CHECKER", AgentConfig.builder()
                .role("代码风格检查员")
                .modelName("qwen-plus")
                .systemPrompt("你是资深代码规范审查专家。审查代码风格、命名规范、注释质量、格式问题。")
                .skills(List.of("JavaCodeStyleSkill"))
                .temperature(0.2)
                .maxSteps(8)
                .build());

        presets.put("EXCEPTION_HANDLER", AgentConfig.builder()
                .role("异常处理专家")
                .modelName("qwen-plus")
                .systemPrompt("你是异常处理专家。审查异常捕获、异常传播、资源关闭、空指针防护等问题。")
                .skills(List.of("ExceptionHandlingSkill"))
                .temperature(0.3)
                .maxSteps(10)
                .build());

        presets.put("ARCHITECT_REVIEWER", AgentConfig.builder()
                .role("架构评审员")
                .modelName("qwen-max")
                .systemPrompt("你是资深架构评审专家。审查架构设计、模块划分、接口设计、设计模式使用、依赖关系等宏观问题。")
                .skills(List.of("JavaCodeStyleSkill", "PerformanceSkill"))
                .temperature(0.4)
                .maxSteps(15)
                .build());

        return presets;
    }
}
