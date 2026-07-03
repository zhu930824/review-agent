package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GovernanceRulePackChangeVO {

    private Long id;

    private String rulePackKey;

    private Long findingId;

    private String changeType;

    private String title;

    private String rationale;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;
}
