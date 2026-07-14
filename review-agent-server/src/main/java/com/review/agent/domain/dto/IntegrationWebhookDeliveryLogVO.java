package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationWebhookDeliveryLogVO {

    private Long id;
    private String connectorKey;
    private String provider;
    private String deliveryId;
    private String eventType;
    private String deliveryStatus;
    private String payloadDigest;
    private String errorMessage;
    private String triggerStatus;
    private String triggerKey;
    private Long triggerReviewId;
    private String triggerMessage;
    private Integer triggerRetryCount;
    private LocalDateTime triggerNextRetryAt;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
