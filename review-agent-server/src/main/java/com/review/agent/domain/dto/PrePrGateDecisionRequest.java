package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrePrGateDecisionRequest {

    @NotBlank
    private String gateStatus;

    @NotBlank
    private String reason;

    @NotBlank
    private String decidedBy;
}
