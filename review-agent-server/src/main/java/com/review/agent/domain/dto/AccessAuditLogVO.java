package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessAuditLogVO {

    private Long id;
    private Long actorUserId;
    private String actorUsername;
    private String actionType;
    private String actionStatus;
    private Long targetUserId;
    private String targetUsername;
    private Long projectId;
    private String previousRole;
    private String newRole;
    private String detail;
    private LocalDateTime createdAt;
}
