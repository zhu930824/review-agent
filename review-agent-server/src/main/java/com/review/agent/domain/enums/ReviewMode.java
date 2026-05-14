package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审查模式
 */
@Getter
public enum ReviewMode {

    SINGLE("SINGLE", "单模型审查"),
    MULTI("MULTI", "多模型交叉审查"),
    JUDGE("JUDGE", "裁判模式"),
    AGENT("AGENT", "多智能体审查");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    ReviewMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
