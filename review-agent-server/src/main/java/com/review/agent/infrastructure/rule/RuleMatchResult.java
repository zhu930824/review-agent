package com.review.agent.infrastructure.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleMatchResult {

    private String ruleKey;
    private String ruleName;
    private String category;
    private String severity;
    private String filePath;
    private Integer lineNumber;
    private String matchedContent;
    private String message;
    private String suggestion;
}
