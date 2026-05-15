package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("model_profile")
public class ModelProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;

    private String profileKey;

    private String modelName;

    private String displayName;

    private String capabilityTags;

    private BigDecimal defaultTemperature;

    private Integer timeoutSeconds;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
