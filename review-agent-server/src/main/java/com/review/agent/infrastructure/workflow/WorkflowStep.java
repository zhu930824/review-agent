package com.review.agent.infrastructure.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {
    private String stepKey;
    private String name;
    private String type;
    private String responsible;
    private int order;
    private String dependsOn;
    private String status;
}
