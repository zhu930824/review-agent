package com.review.agent.domain.exception;

import lombok.Getter;

@Getter
public enum CommonExceptionEnum implements BizStatus {

    SUCCESSFUL(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;

    CommonExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
