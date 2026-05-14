package com.review.agent.infrastructure.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionResult {

    private String role;
    private String modelName;
    private boolean success;
    private String output;
    private String errorMessage;
    private long durationMs;

    public static AgentExecutionResult success(String role, String modelName, String output) {
        return AgentExecutionResult.builder()
                .role(role).modelName(modelName).success(true).output(output)
                .build();
    }

    public static AgentExecutionResult failed(String role, String modelName, String errorMessage) {
        return AgentExecutionResult.builder()
                .role(role).modelName(modelName).success(false).errorMessage(errorMessage)
                .build();
    }
}
