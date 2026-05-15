package com.review.agent.common.exception;

import com.review.agent.domain.exception.BizStatus;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(BizStatus bizStatus) {
        super(bizStatus.getMsg());
        this.code = bizStatus.getCode();
    }

    public BizException(BizStatus bizStatus, Throwable cause) {
        super(bizStatus.getMsg(), cause);
        this.code = bizStatus.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
