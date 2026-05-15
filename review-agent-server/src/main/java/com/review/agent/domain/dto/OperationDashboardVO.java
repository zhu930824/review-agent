package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 运营面板聚合响应 —— 包含所有发现项列表及关键指标摘要
 */
@Data
public class OperationDashboardVO {

    /** 所有审查发现项 */
    private List<OperationFindingVO> findings;

    /** 发现项总数 */
    private int totalFindings;

    /** BLOCKER 严重级别数量 */
    private int blockerCount;

    /** MAJOR 严重级别数量 */
    private int majorCount;

    /** 人工审查状态为 PENDING 的数量 */
    private int pendingCount;

    /** 人工审查状态为 CONFIRMED 的数量 */
    private int confirmedCount;

    /** 人工审查状态为 DISMISSED 的数量 */
    private int dismissedCount;

    /**
     * SLA 压力百分比 —— BLOCKER 发现项占总数的比例（BLOCKER 有 4h SLA）
     * 范围 0~100，无发现项时返回 0
     */
    private int slaPressure;
}
