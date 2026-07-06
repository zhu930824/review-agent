package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class UpdateOperationsTaskRequest {

    private String status;

    private String ownerRole;

    private Long slaHours;
}
