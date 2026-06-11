package com.review.agent.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrePrGateFindingInput {

    private String severity;
    private String humanStatus;
    private String title;
}
