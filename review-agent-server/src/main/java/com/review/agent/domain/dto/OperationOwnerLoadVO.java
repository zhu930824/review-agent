package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationOwnerLoadVO {

    private String role;
    private Long count = 0L;
    private Long percent = 0L;
}
