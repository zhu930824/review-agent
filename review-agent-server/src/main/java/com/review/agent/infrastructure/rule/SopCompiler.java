package com.review.agent.infrastructure.rule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.infrastructure.sop.SopDocument;
import com.review.agent.infrastructure.sop.SopSection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class SopCompiler {

    private final ObjectMapper objectMapper;

    public List<RuleDefinition> compile(SopDocument document) {
        List<RuleDefinition> rules = new ArrayList<>();
        if (document == null || document.getSections() == null) {
            return rules;
        }
        for (SopSection section : document.getSections()) {
            RuleDefinition rule = compileSection(document, section);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }

    private RuleDefinition compileSection(SopDocument doc, SopSection section) {
        if (!section.isActionable()) {
            return null;
        }
        String ruleKey = sanitizeKey(section.getTitle());
        List<RuleCondition> conditions = extractConditions(section.getCheckItems());

        if (conditions.isEmpty()) {
            return null;
        }

        return RuleDefinition.builder()
                .ruleKey("sop-" + ruleKey)
                .name(section.getTitle())
                .category(mapCategory(section.getCategory()))
                .severity(section.getDefaultSeverity() != null ? section.getDefaultSeverity() : "MAJOR")
                .description(section.getDescription())
                .message(section.getViolationMessage())
                .suggestion(section.getFixSuggestion())
                .conditions(conditions)
                .enabled(true)
                .build();
    }

    private List<RuleCondition> extractConditions(List<String> checkItems) {
        List<RuleCondition> conditions = new ArrayList<>();
        if (checkItems == null) return conditions;

        for (String item : checkItems) {
            // 识别 path: pattern 格式
            Matcher pathMatcher = Pattern.compile("path:\\s*(\\S+)").matcher(item);
            String pathPattern = pathMatcher.find() ? pathMatcher.group(1) : "**/*.java";

            // 提取正则模式
            String cleaned = item.replaceAll("path:\\s*\\S+\\s*", "").trim();
            if (!cleaned.isEmpty()) {
                conditions.add(RuleCondition.builder()
                        .type("PATTERN")
                        .pathPattern(pathPattern)
                        .contentPattern(escapeForRegex(cleaned))
                        .build());
            }
        }
        return conditions;
    }

    private String mapCategory(String sopCategory) {
        if (sopCategory == null) return "OTHER";
        return switch (sopCategory.toUpperCase()) {
            case "SECURITY", "安全" -> "SECURITY";
            case "ARCHITECTURE", "架构" -> "ARCHITECTURE";
            case "PERFORMANCE", "性能" -> "PERFORMANCE";
            case "CODE_STYLE", "代码风格" -> "CODE_STYLE";
            default -> "OTHER";
        };
    }

    private String sanitizeKey(String title) {
        if (title == null) return "unknown";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String escapeForRegex(String input) {
        return input.replace(".", "\\.")
                .replace("*", ".*")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
