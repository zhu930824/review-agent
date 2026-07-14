package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JenkinsReviewTriggerResultVO {

    private String status;
    private String triggerKey;
    private Long reviewId;
    private String message;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
}
