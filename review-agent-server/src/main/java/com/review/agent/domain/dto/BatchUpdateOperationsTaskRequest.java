package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateOperationsTaskRequest {

    private List<String> taskKeys;

    private String status;

    private String ownerRole;

    private Long slaHours;
}
