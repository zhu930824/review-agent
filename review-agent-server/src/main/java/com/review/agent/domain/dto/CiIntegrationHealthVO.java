package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CiIntegrationHealthVO {

    private String connectorKey;
    private String provider;
    private String healthStatus;
    private Long totalCount;
    private Long successCount;
    private Long failedCount;
    private Long skippedCount;
    private Long latestWritebackId;
    private String latestWritebackStatus;
    private String latestExternalResult;
    private String latestRequestUrl;
    private String latestExternalQueueUrl;
    private String latestExternalBuildUrl;
    private LocalDateTime latestAt;
    private String summary;
}
