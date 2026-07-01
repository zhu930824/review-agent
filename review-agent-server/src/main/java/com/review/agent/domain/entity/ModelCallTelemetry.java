package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_call_telemetry")
public class ModelCallTelemetry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;
    private String strategyKey;
    private String provider;
    private String modelName;
    private String role;
    private String promptVersion;
    private String status;
    private Integer latencyMs;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer costMicroCents;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
