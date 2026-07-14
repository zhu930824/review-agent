package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectGitLabReviewPolicyRequest {

    @NotNull
    private Boolean autoReviewEnabled;

    @NotNull
    private Boolean reviewDrafts;

    @NotNull
    private Boolean publishSummaryEnabled;

    @Size(max = 500)
    private String targetBranchPattern;
}
