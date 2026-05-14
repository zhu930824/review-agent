package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 人工审查状态
 */
@Getter
public enum HumanStatus {

    PENDING("PENDING", "待处理"),
    CONFIRMED("CONFIRMED", "已确认"),
    DISMISSED("DISMISSED", "已驳回");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    HumanStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
