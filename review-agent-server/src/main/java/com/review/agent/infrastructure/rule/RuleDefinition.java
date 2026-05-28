package com.review.agent.infrastructure.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinition {

    private String ruleKey;
    private String name;
    private String category;
    private String severity;
    private String description;
    private String message;
    private String suggestion;
    private java.util.List<RuleCondition> conditions;
    private Boolean enabled;
}
