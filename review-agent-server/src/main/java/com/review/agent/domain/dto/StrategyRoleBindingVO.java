package com.review.agent.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 策略角色绑定视图对象 —— 描述某个策略中一个角色绑定到哪个模型档案
 */
@Data
public class StrategyRoleBindingVO {

    /** WORKER / JUDGE / Agent 角色 */
    private String role;

    /** 绑定的模型档案 ID */
    private Long modelProfileId;

    /** 模型档案展示名称 */
    private String modelProfileName;

    /** 模型真实名称 */
    private String modelName;

    /** 该角色绑定的技能列表 */
    private List<String> skills;

    /** 角色温度覆盖 */
    private BigDecimal temperature;
}
