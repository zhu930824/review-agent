package com.review.agent.controller;

import com.review.agent.domain.dto.BaseResult;
import com.review.agent.domain.dto.OperationDashboardVO;
import com.review.agent.service.OperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营面板控制器 —— 提供运营数据聚合 API
 */
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;

    /**
     * 获取运营面板数据：包括所有发现项列表及关键指标
     */
    @GetMapping("/dashboard")
    public BaseResult<OperationDashboardVO> getDashboard() {
        return BaseResult.success(operationsService.getDashboard());
    }
}
