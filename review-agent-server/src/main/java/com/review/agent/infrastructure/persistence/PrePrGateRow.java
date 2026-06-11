package com.review.agent.infrastructure.persistence;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrePrGateRow {

    private Long id;
    private Long reviewId;
    private String gateStatus;
    private String summary;
    private String blockedReasonsJson;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
