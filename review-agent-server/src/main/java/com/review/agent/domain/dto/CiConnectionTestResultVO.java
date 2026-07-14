package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CiConnectionTestResultVO {

    private String connectorKey;
    private String provider;
    private String status;
    private String message;
    private String requestUrl;
    private Long latencyMs;
    private LocalDateTime testedAt;
}
