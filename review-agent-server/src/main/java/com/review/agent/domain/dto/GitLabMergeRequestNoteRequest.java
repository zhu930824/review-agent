package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GitLabMergeRequestNoteRequest {

    @NotNull
    private Long reviewId;

    private String mergeRequestIid;

    @NotBlank
    private String body;
}
