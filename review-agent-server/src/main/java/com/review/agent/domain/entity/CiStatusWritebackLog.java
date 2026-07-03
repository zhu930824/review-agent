package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integration_ci_writeback_log")
public class CiStatusWritebackLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorKey;
    private String provider;
    private Long reviewId;
    private String commitSha;
    private String state;
    private String writebackStatus;
    private String requestUrl;
    private String externalQueueUrl;
    private String externalBuildUrl;
    private String externalBuildNumber;
    private String externalBuildResult;
    private LocalDateTime externalResultUpdatedAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
