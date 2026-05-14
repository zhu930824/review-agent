package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    @NotBlank(message = "仓库地址不能为空")
    @Pattern(regexp = "^(https?|git|ssh)://.*$", message = "仓库地址格式不正确")
    private String repoUrl;

    /** 默认分支，缺省 main */
    private String defaultBranch = "main";

    private String description;
}
