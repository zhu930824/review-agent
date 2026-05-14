package com.review.agent.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 80, message = "用户名长度需要在 3 到 80 个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需要在 6 到 100 个字符之间")
    private String password;

    @NotBlank(message = "显示名称不能为空")
    @Size(max = 100, message = "显示名称不能超过 100 个字符")
    private String displayName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 200, message = "邮箱不能超过 200 个字符")
    private String email;
}
