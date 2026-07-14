package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CredentialRotationResultVO {

    private String status;
    private Integer rotatedCredentialCount;
    private String message;
    private LocalDateTime rotatedAt;
}
