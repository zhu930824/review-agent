package com.review.agent.infrastructure.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCondition {

    private String type;
    private String pathPattern;
    private String contentPattern;
    private String filePattern;
    private String language;
    private Integer minLines;
    private Integer maxLines;
}
