package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertCiStatusConfigRequest {

    @NotBlank(message = "连接器标识不能为空")
    private String connectorKey = "github-checks";

    private String displayName;

    private Long projectId;

    @NotBlank(message = "CI 提供方不能为空")
    private String provider = "GITHUB";

    @NotBlank(message = "仓库 Owner 不能为空")
    private String repoOwner;

    @NotBlank(message = "仓库名称不能为空")
    private String repoName;

    private String repoUrl;

    private String defaultBranch = "main";

    @NotBlank(message = "状态检查名称不能为空")
    private String statusContext = "Review Agent";

    private String jenkinsParameterTemplate;

    private String notificationWebhookUrl;

    private Boolean checksEnabled = true;

    private Boolean sarifUploadEnabled = false;

    private String apiToken;

    private String webhookSecret;
}
