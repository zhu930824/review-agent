package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
    private String closeReason;
    private OperationsExternalIssueVO externalIssue;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDueAt;
    private String slaState;
    private Long remainingHours;
}
