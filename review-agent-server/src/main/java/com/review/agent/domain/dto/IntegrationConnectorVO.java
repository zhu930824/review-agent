package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集成连接器视图对象
 */
@Data
public class IntegrationConnectorVO {

    private Long id;
    private String connectorKey;
    private String name;
    private String stage;
    private String status;
    private String businessValue;
    private String implementationHint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
