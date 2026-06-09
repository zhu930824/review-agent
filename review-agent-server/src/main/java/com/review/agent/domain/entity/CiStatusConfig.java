package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integration_ci_config")
public class CiStatusConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorKey;
    private String provider;
    private String repoOwner;
    private String repoName;
    private String repoUrl;
    private String defaultBranch;
    private String statusContext;
    private Boolean checksEnabled;
    private Boolean sarifUploadEnabled;
    private String apiToken;
    private String webhookSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
