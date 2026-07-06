package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class LinkOperationsExternalIssueRequest {

    private String provider;

    private String externalIssueId;

    private String externalIssueIid;

    private String externalIssueUrl;

    private String externalIssueState;

    private String externalIssueTitle;

    private String externalIssueLabels;

    private String externalIssueAssignee;

    private String externalIssueAuthor;
}
