package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pre_pr_gate")
public class PrePrGate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;

    private String gateStatus;

    private String summary;

    private String blockedReasons;

    private String decidedBy;

    private LocalDateTime decidedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
