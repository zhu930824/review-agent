package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationsExternalIssueVO {

    private String taskKey;

    private String provider;

    private String issueStatus;

    private String externalIssueId;

    private String externalIssueIid;

    private String externalIssueUrl;

    private String externalIssueState;

    private String externalIssueTitle;

    private String externalIssueLabels;

    private String externalIssueAssignee;

    private String externalIssueAuthor;

    private LocalDateTime externalUpdatedAt;

    private LocalDateTime externalClosedAt;

    private String requestUrl;

    private String errorMessage;

    private LocalDateTime syncedAt;
}
