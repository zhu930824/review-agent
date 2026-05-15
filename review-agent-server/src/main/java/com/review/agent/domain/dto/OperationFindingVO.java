package com.review.agent.domain.dto;

import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营面板中的单个发现项视图 —— 对应前端 OperationalFinding 类型
 */
@Data
public class OperationFindingVO {

    private Long id;
    private Long reviewId;

    /** 项目名称，通过关联 review -> project 查询 */
    private String projectName;

    private Severity severity;
    private FindingCategory category;
    private String title;
    private HumanStatus humanStatus;

    /** 置信度 0.00~1.00 */
    private BigDecimal confidence;

    /** 是否被多个模型交叉命中 */
    private Boolean isCrossHit;
}
