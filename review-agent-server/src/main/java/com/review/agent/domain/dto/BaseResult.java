package com.review.agent.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.review.agent.domain.exception.BizStatus;
import com.review.agent.domain.exception.CommonExceptionEnum;
import io.micrometer.common.util.StringUtils;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResult<T> implements Serializable, BizStatus {

    protected T data;
    protected int code;
    protected String msg;
    protected String tips;

    public BaseResult() {
        this.code = CommonExceptionEnum.SUCCESSFUL.getCode();
        this.msg = CommonExceptionEnum.SUCCESSFUL.getMsg();
    }

    protected BaseResult(T data) {
        this();
        this.data = data;
    }

    public static <T> BaseResult<T> success() {
        return new BaseResult<>();
    }

    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<>(data);
    }

    public static <T> BaseResult<T> error(BizStatus bizStatus) {
        BaseResult<T> baseResult = new BaseResult<>();
        baseResult.code = bizStatus.getCode();
        baseResult.msg = bizStatus.getMsg();
        return baseResult;
    }

    public static <T> BaseResult<T> error(BizStatus bizStatus, String cause) {
        BaseResult<T> error = new BaseResult<>();
        error.code = bizStatus.getCode();
        error.msg = StringUtils.isBlank(cause) ? bizStatus.getMsg() : cause;
        error.tips = cause == null ? "" : cause;
        return error;
    }

    public static <T> BaseResult<T> error(int code, String cause) {
        BaseResult<T> error = new BaseResult<>();
        error.code = code;
        error.tips = cause;
        error.msg = cause;
        return error;
    }

    public static <T> BaseResult<T> custom(T data, int code, String msg, String tips, BizStatus bizStatus) {
        BaseResult<T> baseResult = new BaseResult<>();
        baseResult.data = data;
        baseResult.code = code;
        baseResult.msg = msg;
        baseResult.tips = tips;
        return baseResult;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public Boolean requestSuccess() {
        return CommonExceptionEnum.SUCCESSFUL.getCode() == this.code;
    }

    public T getData() {
        return this.data;
    }

    public String getTips() {
        return this.tips;
    }
}
