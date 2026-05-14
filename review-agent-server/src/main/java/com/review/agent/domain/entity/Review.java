package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.review.agent.domain.enums.ReviewMode;
import com.review.agent.domain.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码审查任务实体
 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的项目 ID */
    private Long projectId;

    /** 源分支 */
    private String sourceBranch;

    /** 目标分支 */
    private String targetBranch;

    /** 源 commit SHA */
    private String sourceCommit;

    /** 目标 commit SHA */
    private String targetCommit;

    /** 审查状态 */
    private ReviewStatus status;

    /** 审查模式：单模型 / 多模型 / 裁判模式 */
    private ReviewMode reviewMode;

    /** JSON 格式的模型配置 */
    private String modelsConfig;

    /** 审查摘要 JSON */
    private String summary;

    /** diff 原始内容 */
    private String diffContent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
