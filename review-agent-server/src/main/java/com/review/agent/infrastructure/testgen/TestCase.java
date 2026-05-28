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
public class TestCase {
    private String testName;
    private String targetMethod;
    private String testLevel;
    private String riskLevel;
    private List<String> inputs;
    private String expectedOutput;
    private String preconditions;
    private String testCategory;
}
