package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrePrDecisionRequest {

    @NotBlank(message = "决策状态不能为空")
    private String decision;

    private String comment;
}
