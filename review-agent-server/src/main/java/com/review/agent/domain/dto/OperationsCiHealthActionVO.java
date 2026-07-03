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
    private String latestSignal;
    private String recommendation;
}
