package com.review.agent.domain.dto;

import com.review.agent.domain.enums.ReviewMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private String sourceBranch;

    private String targetBranch;

    private String sourceCommit;

    private String targetCommit;

    /** 默认单模型审查 */
    @NotNull(message = "审查模式不能为空")
    private ReviewMode reviewMode = ReviewMode.SINGLE;

    /** JSON 格式的模型配置 */
    private String modelsConfig;
}
