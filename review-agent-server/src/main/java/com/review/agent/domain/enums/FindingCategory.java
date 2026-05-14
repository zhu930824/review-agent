package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 发现项类别
 */
@Getter
public enum FindingCategory {

    CODE_STYLE("CODE_STYLE", "代码风格"),
    BUG("BUG", "潜在缺陷"),
    PERFORMANCE("PERFORMANCE", "性能问题"),
    SECURITY("SECURITY", "安全问题"),
    EXCEPTION_HANDLING("EXCEPTION_HANDLING", "异常处理"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    FindingCategory(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
