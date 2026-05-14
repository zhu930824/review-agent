package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review_strategy_model")
public class ReviewStrategyModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long strategyId;

    private Long modelProfileId;

    private String role;

    private String skills;

    private BigDecimal temperature;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
