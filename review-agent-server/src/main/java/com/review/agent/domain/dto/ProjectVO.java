package com.review.agent.domain.dto;

import com.review.agent.domain.enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目视图对象
 */
@Data
public class ProjectVO {

    private Long id;
    private String name;
    private String repoUrl;
    private String defaultBranch;
    private String localPath;
    private ProjectStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
