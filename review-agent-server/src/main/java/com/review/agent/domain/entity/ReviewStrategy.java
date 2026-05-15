package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_strategy")
public class ReviewStrategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String strategyKey;

    private String name;

    private String reviewMode;

    private String description;

    private String recommendedFor;

    private String blockOn;

    private String requireHumanReviewOn;

    private String advisoryOn;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
