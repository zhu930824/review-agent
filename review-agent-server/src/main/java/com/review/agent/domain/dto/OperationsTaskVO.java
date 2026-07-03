package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationsTaskVO {

    private String taskKey;
    private String sourceType;
    private String sourceId;
    private String sourceRef;
    private String title;
    private String status;
    private String severity;
    private String ownerRole;
    private Long slaHours;
    private Integer priorityScore;
    private String latestSignal;
    private String recommendation;
}
