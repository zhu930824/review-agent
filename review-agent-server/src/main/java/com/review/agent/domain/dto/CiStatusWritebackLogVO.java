package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CiStatusWritebackLogVO {

    private Long id;
    private String connectorKey;
    private String provider;
    private Long reviewId;
    private String commitSha;
    private String state;
    private String writebackStatus;
    private String requestUrl;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
