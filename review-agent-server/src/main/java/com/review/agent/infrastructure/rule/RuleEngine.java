package com.review.agent.infrastructure.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.diff.DiffLine;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.dto.diff.Hunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final ObjectMapper objectMapper;

    public List<RuleMatchResult> evaluate(List<FileChange> fileChanges, List<RuleDefinition> rules) {
        if (rules == null || rules.isEmpty() || fileChanges == null || fileChanges.isEmpty()) {
            return List.of();
        }

        List<RuleMatchResult> results = new ArrayList<>();
        for (FileChange file : fileChanges) {
            String filePath = file.getFilePath();
            if (filePath == null) {
                continue;
            }
            for (RuleDefinition rule : rules) {
                if (!Boolean.FALSE.equals(rule.getEnabled())
                        && rule.getConditions() != null) {
                    results.addAll(evaluateFile(file, rule));
                }
            }
        }
        return results;
    }

    private List<RuleMatchResult> evaluateFile(FileChange file, RuleDefinition rule) {
        String filePath = file.getFilePath();
        List<RuleMatchResult> matches = new ArrayList<>();
        for (RuleCondition condition : rule.getConditions()) {
            if (!matchesPath(filePath, condition)) {
                continue;
            }
            matches.addAll(evaluateCondition(file, rule, condition));
        }
        return matches;
    }

    private List<RuleMatchResult> evaluateCondition(FileChange file, RuleDefinition rule, RuleCondition condition) {
        List<RuleMatchResult> results = new ArrayList<>();
        if (!"PATTERN".equalsIgnoreCase(condition.getType())) {
            return results;
        }
        String pattern = condition.getContentPattern();
        if (pattern == null) {
            return results;
        }
        Pattern regex = Pattern.compile(pattern);
        if (file.getHunks() == null) {
            return results;
        }
        for (Hunk hunk : file.getHunks()) {
            if (hunk.getLines() == null) continue;
            for (DiffLine line : hunk.getLines()) {
                if (line.getContent() == null) continue;
                if (regex.matcher(line.getContent()).find()) {
                    results.add(RuleMatchResult.builder()
                            .ruleKey(rule.getRuleKey())
                            .ruleName(rule.getName())
                            .category(rule.getCategory())
                            .severity(rule.getSeverity())
                            .filePath(file.getFilePath())
                            .lineNumber(line.getLineNumber())
                            .matchedContent(line.getContent().trim())
                            .message(rule.getMessage())
                            .suggestion(rule.getSuggestion())
                            .build());
                }
            }
        }
        return results;
    }

    private boolean matchesPath(String filePath, RuleCondition condition) {
        if (condition.getPathPattern() == null) {
            return true;
        }
        String pattern = condition.getPathPattern()
                .replace("**", ".*")
                .replace("*", "[^/]*")
                .replace("/", "\\/");
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(filePath)
                .find();
    }
}
