package com.review.agent.infrastructure.agent;

import com.review.agent.domain.dto.diff.DiffLine;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.dto.diff.Hunk;
import com.review.agent.infrastructure.agent.config.AgentConfig;
import com.review.agent.infrastructure.agent.skill.SkillRegistry;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Spring AI Alibaba 原生 ReactAgent 封装
 * 运行 ReAct 循环：Thought → Action(调用 Tool) → Observation → ... → Final Answer
 */
@Slf4j
@Getter
public class ReviewAgent {

    private final AgentConfig config;
    private final ReactAgent agent;

    public ReviewAgent(AgentConfig config, ChatModel chatModel,
                       List<ToolCallback> tools, SkillRegistry skillRegistry) {
        this.config = config;

        // 将技能知识拼入系统提示词
        String skillKnowledge = skillRegistry.getKnowledgeRules(config.getSkills());
        String fullSystemPrompt = config.getSystemPrompt() + "\n\n" + skillKnowledge;

        this.agent = ReactAgent.builder()
                .name(sanitizeName(config.getRole()))
                .model(chatModel)
                .systemPrompt(fullSystemPrompt)
                .tools(tools.toArray(new ToolCallback[0]))
                .build();
    }

    /** 执行 ReAct 审查，返回最终输出文本 */
    public AgentExecutionResult executeReview(Long reviewId, List<FileChange> fileChanges) {
        String taskPrompt = buildReviewPrompt(reviewId, fileChanges);

        try {
            AssistantMessage response = agent.call(taskPrompt);
            String output = response.getText();
            return AgentExecutionResult.success(config.getRole(), config.getModelName(), output);
        } catch (Exception e) {
            log.error("Agent 执行失败: role={}", config.getRole(), e);
            return AgentExecutionResult.failed(config.getRole(), config.getModelName(), e.getMessage());
        }
    }

    /** 角色名中的特殊字符替换为下划线，确保 name 合法 */
    private String sanitizeName(String role) {
        return role.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private String buildReviewPrompt(Long reviewId, List<FileChange> fileChanges) {
        StringBuilder sb = new StringBuilder();
        sb.append("审查任务ID: ").append(reviewId).append("\n\n");
        sb.append("请对以下代码变更进行审查：\n\n");

        for (FileChange fc : fileChanges) {
            sb.append("--- 文件: ").append(fc.getFilePath())
                    .append(" (").append(fc.getChangeType()).append(") ---\n");
            for (Hunk hunk : fc.getHunks()) {
                sb.append("@@ -").append(hunk.getOldStart()).append(",")
                        .append(hunk.getOldLines()).append(" +").append(hunk.getNewStart())
                        .append(",").append(hunk.getNewLines()).append(" @@\n");
                for (DiffLine line : hunk.getLines()) {
                    String prefix = switch (line.getType()) {
                        case ADDED -> "+";
                        case REMOVED -> "-";
                        case CONTEXT -> " ";
                    };
                    sb.append(prefix).append(line.getContent()).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("""
                审查流程：
                1. 先使用 gitLog 工具了解文件的变更历史（如果需要）
                2. 使用 readFile 工具查看完整文件内容获取上下文（如果需要）
                3. 分析代码变更，发现问题
                4. 对于每个发现的问题，使用 saveFinding 工具保存到数据库
                5. 最后总结本次审查的结果
                
                注意：category 取值 CODE_STYLE/BUG/PERFORMANCE/SECURITY/EXCEPTION_HANDLING/OTHER
                severity 取值 BLOCKER/MAJOR/MINOR/INFO
                """);

        return sb.toString();
    }
}
