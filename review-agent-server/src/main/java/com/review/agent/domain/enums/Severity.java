package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 严重程度
 */
@Getter
public enum Severity {

    BLOCKER("BLOCKER", "阻断"),
    MAJOR("MAJOR", "主要"),
    MINOR("MINOR", "次要"),
    INFO("INFO", "建议");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    Severity(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
