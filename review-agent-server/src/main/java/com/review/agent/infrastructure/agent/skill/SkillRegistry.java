package com.review.agent.infrastructure.agent.skill;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SkillRegistry {

    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public SkillRegistry(List<Skill> skillBeans) {
        for (Skill skill : skillBeans) {
            skills.put(skill.getName(), skill);
        }
    }

    @PostConstruct
    public void init() {
        log.info("已注册 {} 个技能: {}", skills.size(), skills.keySet());
    }

    /** 获取所有已注册的技能 */
    public List<Skill> getAllSkills() {
        return List.copyOf(skills.values());
    }

    /** 根据名称获取技能 */
    public Skill getSkill(String name) {
        return skills.get(name);
    }

    /**
     * 获取指定技能的规则文本，拼接后用于注入 System Prompt。
     *
     * @param skillNames 技能名称列表
     * @return 拼接后的知识规则文本；skillNames 为空时返回空字符串
     */
    public String getKnowledgeRules(List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String name : skillNames) {
            Skill skill = skills.get(name);
            if (skill != null && skill.isEnabled()) {
                sb.append("\n\n## ").append(skill.getDescription()).append("\n\n");
                sb.append(skill.getKnowledgeRules());
            }
        }
        return sb.toString();
    }
}
