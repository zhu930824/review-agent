package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationWebhookDeliveryResultVO {

    private String deliveryId;
    private String eventType;
    private String status;
    private Boolean duplicate;
    private String triggerStatus;
    private String triggerKey;
    private Long triggerReviewId;
    private String triggerMessage;
    private Integer triggerRetryCount;
    private LocalDateTime triggerNextRetryAt;
}
