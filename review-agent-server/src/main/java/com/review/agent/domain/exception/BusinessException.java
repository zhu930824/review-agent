package com.review.agent.domain.exception;

import lombok.Getter;

/**
 * 业务异常，由 GlobalExceptionHandler 统一拦截处理
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，便于前端/调用方归类处理 */
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
