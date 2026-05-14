package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePrePrRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "源分支不能为空")
    private String sourceBranch;

    @NotBlank(message = "目标分支不能为空")
    private String targetBranch;
}
