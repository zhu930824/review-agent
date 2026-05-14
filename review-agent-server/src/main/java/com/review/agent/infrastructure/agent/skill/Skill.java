package com.review.agent.infrastructure.agent.skill;

/**
 * 技能接口：封装特定领域的审查知识和规则。
 * 每个 Skill 代表一组可复用的审查能力，其知识规则会被注入到 Agent 的 System Prompt 中。
 */
public interface Skill {

    /** 技能名称 */
    String getName();

    /** 技能描述 */
    String getDescription();

    /** 技能的核心知识规则文本，会被注入到 Agent 的 System Prompt 中 */
    String getKnowledgeRules();

    /** 是否启用，默认启用 */
    default boolean isEnabled() {
        return true;
    }
}
