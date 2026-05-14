package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 模型在审查中的角色
 */
@Getter
public enum ModelRole {

    WORKER("WORKER", "审查执行者"),
    JUDGE("JUDGE", "裁判模型");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    ModelRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
