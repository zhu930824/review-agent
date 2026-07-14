package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.review.agent.infrastructure.security.CredentialStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GitLab 集成配置 —— 项目级别的 GitLab API 访问凭证。
 * 当配置了 GitLab 凭据时，系统通过 GitLab REST API 直接获取 diff，
 * 无需在本地克隆仓库。
 */
@Data
@TableName(value = "project_gitlab_config", autoResultMap = true)
public class ProjectGitLabConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的 project.id */
    private Long projectId;

    /** GitLab 实例地址，如 https://gitlab.com 或 https://gitlab.example.com */
    private String gitlabHost;

    /** GitLab Personal Access Token（需具备 read_api + read_repository 权限） */
    @TableField(typeHandler = CredentialStringTypeHandler.class)
    private String gitlabToken;

    /** GitLab 项目路径（URL 编码），如 group%2Fproject */
    private String projectPath;

    /** 是否启用 API 模式获取 diff */
    private Boolean enabled;

    private Boolean autoReviewEnabled;

    private Boolean reviewDrafts;

    private Boolean publishSummaryEnabled;

    private String targetBranchPattern;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
