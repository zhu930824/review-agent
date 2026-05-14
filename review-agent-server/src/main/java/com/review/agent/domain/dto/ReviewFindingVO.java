package com.review.agent.domain.dto;

import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审查发现项视图对象
 */
@Data
public class ReviewFindingVO {

    private Long id;
    private Long reviewId;
    private String filePath;
    private Integer lineStart;
    private Integer lineEnd;
    private FindingCategory category;
    private Severity severity;
    private String title;
    private String description;
    private String suggestion;
    private String modelName;
    private BigDecimal confidence;
    private Boolean isCrossHit;
    private HumanStatus humanStatus;
    private LocalDateTime createdAt;
}
