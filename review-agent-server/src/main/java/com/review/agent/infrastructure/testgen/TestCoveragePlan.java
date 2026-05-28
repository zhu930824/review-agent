package com.review.agent.infrastructure.testgen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCoveragePlan {
    private String reviewId;
    private int totalInterfaces;
    private int highRiskCount;
    private int mediumRiskCount;
    private int lowRiskCount;
    private List<TestCase> testCases;
    private List<String> uncoveredPaths;
    private String coverageSummary;
}
