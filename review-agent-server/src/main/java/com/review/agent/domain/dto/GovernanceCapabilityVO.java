package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 治理能力目录视图对象
 */
@Data
public class GovernanceCapabilityVO {

    private Long id;
    private String capabilityKey;
    private String name;
    private String category;
    private String status;
    private String businessImpact;
    private Integer readiness;
    private String marketSignal;
    private String platformMove;

    /** 对标产品列表，由 Service 层从 JSON 字符串解析 */
    private List<String> sourceProducts;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
