package com.review.agent.infrastructure.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {
    private String taskId;
    private String taskName;
    private String status;
    private String assignedTo;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
