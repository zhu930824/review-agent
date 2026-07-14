package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JenkinsReviewTriggerRetryRequest {

    private String connectorKey = "jenkins-pipeline";

    @NotBlank
    private String triggerKey;
}
