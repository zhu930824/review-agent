package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CiStatusConfigVO {

    private Long id;
    private String connectorKey;
    private String displayName;
    private Long projectId;
    private String provider;
    private String repoOwner;
    private String repoName;
    private String repoUrl;
    private String defaultBranch;
    private String statusContext;
    private String jenkinsParameterTemplate;
    private String notificationWebhookUrl;
    private Boolean checksEnabled;
    private Boolean sarifUploadEnabled;
    private Boolean tokenConfigured;
    private Boolean webhookSecretConfigured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
