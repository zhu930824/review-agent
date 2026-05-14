package com.review.agent.infrastructure.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JudgeReviewResult {
    private List<ModelReviewResult> workerResults;
    private JudgeEvaluation evaluation;
}
