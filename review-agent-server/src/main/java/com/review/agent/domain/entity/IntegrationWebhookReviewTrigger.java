package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integration_webhook_review_trigger")
public class IntegrationWebhookReviewTrigger {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorKey;
    private String triggerKey;
    private String triggerStatus;
    private Long projectId;
    private String sourceBranch;
    private String targetBranch;
    private String mergeRequestIid;
    private String mergeRequestUrl;
    private String externalEventId;
    private String externalEventUrl;
    private String commitSha;
    private Long reviewId;
    private String message;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
