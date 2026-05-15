package com.review.agent.common.result;

import com.review.agent.domain.exception.BizStatus;
import com.review.agent.domain.exception.CommonExceptionEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(CommonExceptionEnum.SUCCESSFUL.getCode());
        result.setMessage(CommonExceptionEnum.SUCCESSFUL.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(CommonExceptionEnum.INTERNAL_ERROR.getCode(), message);
    }

    public static <T> Result<T> fail(BizStatus bizStatus) {
        return fail(bizStatus.getCode(), bizStatus.getMsg());
    }

    public static <T> Result<T> fail(BizStatus bizStatus, String message) {
        return fail(bizStatus.getCode(), message);
    }

    public boolean isSuccess() {
        return CommonExceptionEnum.SUCCESSFUL.getCode() == this.code;
    }
}
