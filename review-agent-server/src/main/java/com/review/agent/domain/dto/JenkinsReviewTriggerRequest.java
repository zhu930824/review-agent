package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JenkinsReviewTriggerRequest {

    @NotNull
    private Long projectId;

    @NotBlank
    private String sourceBranch;

    @NotBlank
    private String targetBranch;

    @NotBlank
    private String jobName;

    @NotBlank
    private String buildNumber;

    private String buildUrl;
    private String commitSha;
}
