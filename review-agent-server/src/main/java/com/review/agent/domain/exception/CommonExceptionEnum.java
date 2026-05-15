package com.review.agent.domain.exception;

import lombok.Getter;

@Getter
public enum CommonExceptionEnum implements BizStatus {

    SUCCESSFUL(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // Auth 401xx
    AUTH_TOKEN_INVALID(40101, "登录凭证无效"),
    AUTH_TOKEN_EXPIRED(40102, "登录已过期"),
    AUTH_INVALID_CREDENTIALS(40103, "用户名或密码错误"),
    AUTH_USER_DISABLED(40104, "账号已停用"),
    AUTH_USERNAME_EXISTS(40105, "用户名已存在"),
    AUTH_EMAIL_EXISTS(40106, "邮箱已存在"),

    // Project 400xx
    PROJECT_NOT_FOUND(40001, "项目不存在"),
    REPO_NOT_CLONED(40002, "仓库尚未克隆完成"),
    PROJECT_RETRY_NOT_ALLOWED(40003, "只有克隆失败的项目才能重新克隆"),

    // Review 402xx
    REVIEW_NOT_FOUND(40201, "审查记录不存在"),
    REVIEW_FINDING_NOT_FOUND(40202, "审查发现不存在"),
    INVALID_REVIEW_PARAMS(40203, "必须指定分支或commit范围"),
    REVIEW_FAILED(40204, "审查执行失败"),
    INVALID_MODELS_CONFIG(40205, "模型配置格式错误"),

    // ModelConfig 403xx
    MODEL_PROVIDER_NOT_FOUND(40301, "模型供应商不存在"),
    MODEL_PROFILE_NOT_FOUND(40302, "模型档案不存在"),
    REVIEW_STRATEGY_NOT_FOUND(40303, "审查策略不存在"),

    // Git 500xx
    GIT_CLONE_FAILED(50001, "仓库克隆失败"),
    GIT_PULL_FAILED(50002, "仓库拉取失败"),
    GIT_FETCH_FAILED(50003, "仓库fetch失败"),
    GIT_DIFF_FAILED(50004, "获取diff失败"),
    GIT_BRANCH_FAILED(50005, "获取分支列表失败"),
    GIT_RESOLVE_FAILED(50006, "解析分支或提交失败"),
    GIT_OPEN_FAILED(50007, "打开仓库失败");

    private final int code;
    private final String msg;

    CommonExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
