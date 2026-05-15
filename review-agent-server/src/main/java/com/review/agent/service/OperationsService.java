package com.review.agent.service;

import com.review.agent.domain.dto.OperationDashboardVO;

/**
 * 运营面板服务 —— 聚合 review_finding、review、project 数据
 * 提供关键运营指标
 */
public interface OperationsService {

    /**
     * 获取运营面板完整数据
     *
     * @return 包含发现项列表及核心指标摘要的面板对象
     */
    OperationDashboardVO getDashboard();
}
