package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GitLabReviewTriggerRetryRequest {

    @NotBlank
    private String triggerKey;
}
