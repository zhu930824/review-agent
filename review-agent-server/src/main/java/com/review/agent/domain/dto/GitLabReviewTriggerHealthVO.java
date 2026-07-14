package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GitLabReviewTriggerHealthVO {

    private String healthStatus;
    private Long processingCount;
    private Long failedCount;
    private Long exhaustedCount;
    private Long processedCount;
    private LocalDateTime oldestPendingAt;
    private String summary;
}
