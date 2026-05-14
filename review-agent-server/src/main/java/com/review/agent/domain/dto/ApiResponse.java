package com.review.agent.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应体，所有 Controller 均通过此类包装返回
 *
 * @param <T> 负载数据类型
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 业务状态码 */
    private final String code;

    /** 提示信息 */
    private final String message;

    /** 负载数据 */
    private final T data;

    // ---- 静态工厂方法 ----

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("200", "success", null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
