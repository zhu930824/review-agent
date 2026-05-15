package com.review.agent.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 模型档案视图对象
 */
@Data
public class ModelProfileVO {

    private Long id;
    private Long providerId;
    private String profileKey;
    private String modelName;
    private String displayName;

    /** 能力标签，从 JSON 数组字符串解析而来 */
    private List<String> capabilityTags;

    private BigDecimal defaultTemperature;
    private Integer timeoutSeconds;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
