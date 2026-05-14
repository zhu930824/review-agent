package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审查发现项实体 —— 记录模型发现的每一个问题
 */
@Data
@TableName("review_finding")
public class ReviewFinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的审查任务 ID */
    private Long reviewId;

    /** 问题所在文件路径 */
    private String filePath;

    /** 问题起始行号 */
    private Integer lineStart;

    /** 问题结束行号 */
    private Integer lineEnd;

    /** 问题类别 */
    private FindingCategory category;

    /** 严重程度 */
    private Severity severity;

    /** 问题标题 */
    private String title;

    /** 问题描述 */
    private String description;

    /** 改进建议 */
    private String suggestion;

    /** 来源模型名称 */
    private String modelName;

    /** 置信度 0.00~1.00 */
    private BigDecimal confidence;

    /** 是否被多个模型交叉命中 */
    private Boolean isCrossHit;

    /** 人工审查状态 */
    private HumanStatus humanStatus;

    private LocalDateTime createdAt;
}
