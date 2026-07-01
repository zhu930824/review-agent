package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integration_action_log")
public class IntegrationActionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorKey;
    private String provider;
    private String actionType;
    private String actionStatus;
    private String targetKey;
    private String commitSha;
    private String requestUrl;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
