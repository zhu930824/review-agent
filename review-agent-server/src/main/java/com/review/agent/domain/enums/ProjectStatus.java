package com.review.agent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 项目状态
 */
@Getter
public enum ProjectStatus {

    PENDING("PENDING", "待处理"),
    CLONING("CLONING", "克隆中"),
    READY("READY", "就绪"),
    ERROR("ERROR", "异常");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    ProjectStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
