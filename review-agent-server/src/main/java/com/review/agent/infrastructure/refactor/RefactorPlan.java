package com.review.agent.infrastructure.refactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefactorPlan {
    private String planId;
    private String title;
    private String summary;
    private int totalSteps;
    private int estimatedImpact;
    private List<RefactorStep> steps;
    private List<String> prerequisites;
    private List<String> risks;
}
