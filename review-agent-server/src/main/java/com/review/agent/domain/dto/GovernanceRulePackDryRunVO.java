package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class GovernanceRulePackDryRunVO {

    private Long changeId;

    private String rulePackKey;

    private String changeType;

    private String title;

    private Integer existingControlCount;

    private Integer proposedControlCount;

    private List<String> proposedControls;

    private String controlsSnapshot;

    private List<String> impactSummary;
}
