package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integration_webhook_delivery_log")
public class IntegrationWebhookDeliveryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorKey;
    private String provider;
    private String deliveryId;
    private String eventType;
    private String deliveryStatus;
    private String signature;
    private String payloadDigest;
    private String errorMessage;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
