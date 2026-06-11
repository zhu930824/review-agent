package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PrePrGateVO {

    private Long id;
    private Long reviewId;
    private String gateStatus;
    private String summary;
    private List<String> blockedReasons = new ArrayList<>();
    private String decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
