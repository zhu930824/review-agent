package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationsCiHealthActionVO {

    private String key;
    private String connectorKey;
    private String provider;
    private String healthStatus;
    private String severity;
    private String ownerRole;
    private Long slaHours;
    private Long latestWritebackId;
    private String latestWritebackStatus;
    private String latestSignal;
    private String latestRequestUrl;
    private String latestExternalQueueUrl;
    private String latestExternalBuildUrl;
    private String recommendation;
    private String notificationPriority;
    private String notificationDedupKey;
    private String notificationTitle;
    private String notificationBody;
    private String notificationTargetUrl;
}
