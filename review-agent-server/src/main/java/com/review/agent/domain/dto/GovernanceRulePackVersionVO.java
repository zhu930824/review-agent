package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GovernanceRulePackVersionVO {

    private Long id;

    private String rulePackKey;

    private Long sourceChangeId;

    private Integer versionNo;

    private String versionStatus;

    private String title;

    private String controlsSnapshot;

    private String rationale;

    private String createdBy;

    private LocalDateTime createdAt;
}
