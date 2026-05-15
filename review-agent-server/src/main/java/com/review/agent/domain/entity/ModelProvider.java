package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型供应商实体
 */
@Data
@TableName("model_provider")
public class ModelProvider {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商唯一标识 */
    private String providerKey;

    /** 供应商名称 */
    private String name;

    /** DASHSCOPE / OPENAI_COMPATIBLE / CUSTOM */
    private String providerType;

    /** API endpoint */
    private String endpoint;

    /** API Key 环境变量名 */
    private String apiKeyEnv;

    /** 是否启用 */
    private Boolean enabled;

    /** 说明 */
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
