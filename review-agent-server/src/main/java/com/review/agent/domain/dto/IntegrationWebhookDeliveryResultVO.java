package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class IntegrationWebhookDeliveryResultVO {

    private String deliveryId;
    private String eventType;
    private String status;
    private Boolean duplicate;
}
