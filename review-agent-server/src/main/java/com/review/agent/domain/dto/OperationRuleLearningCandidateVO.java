package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationRuleLearningCandidateVO {

    private Long findingId;

    private String action;

    private String ruleTitle;

    private String reason;
}
