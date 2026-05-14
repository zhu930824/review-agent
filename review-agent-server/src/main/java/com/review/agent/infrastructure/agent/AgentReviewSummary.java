package com.review.agent.infrastructure.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentReviewSummary {
    private int totalAgents;
    private int successCount;
    private int failedCount;
    private List<AgentExecutionResult> agentResults;
    private String summary;
}
