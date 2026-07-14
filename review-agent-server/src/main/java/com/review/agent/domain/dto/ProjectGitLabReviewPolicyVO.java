package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class ProjectGitLabReviewPolicyVO {

    private Long projectId;
    private Boolean configured;
    private Boolean autoReviewEnabled;
    private Boolean reviewDrafts;
    private Boolean publishSummaryEnabled;
    private String targetBranchPattern;
}
