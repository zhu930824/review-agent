package com.review.agent.infrastructure.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ModelReviewResult {
    private String modelName;
    private List<ReviewFindingResult> findings;
}
