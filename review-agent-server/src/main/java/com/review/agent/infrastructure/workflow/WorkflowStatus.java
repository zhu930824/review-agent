package com.review.agent.infrastructure.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatus {
    private String workflowId;
    private String workflowKey;
    private String status;
    private List<TaskStatus> tasks;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
