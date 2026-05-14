package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审查/任务执行状态（review 表与 review_model_result 表共用）
 */
@Getter
public enum ReviewStatus {

    PENDING("PENDING", "待处理"),
    RUNNING("RUNNING", "执行中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "失败");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    ReviewStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
