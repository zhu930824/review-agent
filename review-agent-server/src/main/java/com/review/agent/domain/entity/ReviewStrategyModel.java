package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审查策略模型绑定实体
 */
@Data
@TableName("review_strategy_model")
public class ReviewStrategyModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 策略 ID */
    private Long strategyId;

    /** 模型档案 ID */
    private Long modelProfileId;

    /** WORKER / JUDGE / Agent 角色 */
    private String role;

    /** Skill 列表 JSON 数组字符串 */
    private String skills;

    /** 角色温度覆盖 */
    private BigDecimal temperature;

    /** 排序 */
    private Integer sortOrder;

    private LocalDateTime createdAt;
}
