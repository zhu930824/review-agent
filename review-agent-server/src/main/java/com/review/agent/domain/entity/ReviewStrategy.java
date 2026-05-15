package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审查策略实体
 */
@Data
@TableName("review_strategy")
public class ReviewStrategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 策略唯一标识 */
    private String strategyKey;

    /** 策略名称 */
    private String name;

    /** SINGLE / MULTI / JUDGE / AGENT */
    private String reviewMode;

    /** 说明 */
    private String description;

    /** 适用场景 JSON 数组字符串 */
    private String recommendedFor;

    /** 阻断级别 JSON 数组字符串，如 ["BLOCKER"] */
    private String blockOn;

    /** 人工复核级别 JSON 数组字符串，如 ["MAJOR"] */
    private String requireHumanReviewOn;

    /** 建议关注级别 JSON 数组字符串，如 ["MINOR","INFO"] */
    private String advisoryOn;

    /** 是否启用 */
    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
