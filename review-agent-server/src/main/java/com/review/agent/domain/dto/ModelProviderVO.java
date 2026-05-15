package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型供应商视图对象
 */
@Data
public class ModelProviderVO {

    private Long id;
    private String providerKey;
    private String name;
    private String kind;
    private String endpoint;
    private String apiKeyEnv;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
