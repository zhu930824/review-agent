package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型档案实体
 */
@Data
@TableName("model_profile")
public class ModelProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商 ID */
    private Long providerId;

    /** 模型档案唯一标识 */
    private String profileKey;

    /** 模型真实名称 */
    private String modelName;

    /** 展示名称 */
    private String displayName;

    /** 能力标签 JSON 数组字符串 */
    private String capabilityTags;

    /** 默认温度 */
    private BigDecimal defaultTemperature;

    /** 超时时间（秒） */
    private Integer timeoutSeconds;

    /** 是否启用 */
    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
