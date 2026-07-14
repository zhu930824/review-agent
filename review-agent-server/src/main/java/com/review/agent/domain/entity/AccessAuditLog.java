package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("access_audit_log")
public class AccessAuditLog {

    @TableId(type = IdType.AUTO)
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
