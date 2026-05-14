package com.review.agent.domain.dto;

import com.review.agent.domain.enums.ReviewMode;
import com.review.agent.domain.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审查任务视图对象
 */
@Data
public class ReviewVO {

    private Long id;
    private Long projectId;
    private String projectName;
    private String sourceBranch;
    private String targetBranch;
    private String sourceCommit;
    private String targetCommit;
    private ReviewStatus status;
    private ReviewMode reviewMode;
    private String modelsConfig;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
