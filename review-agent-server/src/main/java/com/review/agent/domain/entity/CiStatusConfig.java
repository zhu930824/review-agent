package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.review.agent.infrastructure.security.CredentialStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "integration_ci_config", autoResultMap = true)
public class CiStatusConfig {

    @TableId(type = IdType.AUTO)
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
    @TableField(typeHandler = CredentialStringTypeHandler.class)
    private String apiToken;

    @TableField(typeHandler = CredentialStringTypeHandler.class)
    private String webhookSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
