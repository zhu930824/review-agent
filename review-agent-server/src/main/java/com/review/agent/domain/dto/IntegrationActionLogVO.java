package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationActionLogVO {

    private Long id;
    private String connectorKey;
    private String provider;
    private String actionType;
    private String actionStatus;
    private String targetKey;
    private String commitSha;
    private String requestUrl;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
