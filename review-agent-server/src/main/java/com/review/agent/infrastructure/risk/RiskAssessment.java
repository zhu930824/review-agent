package com.review.agent.infrastructure.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    private String level;
    private int score;
    private List<String> factors;
    private List<String> fileLevelRisks;
    private String recommendation;
}
