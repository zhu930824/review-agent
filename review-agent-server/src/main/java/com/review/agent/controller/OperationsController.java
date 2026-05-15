package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.OperationDashboardVO;
import com.review.agent.service.OperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;

    @GetMapping("/dashboard")
    public Result<OperationDashboardVO> getDashboard() {
        return Result.success(operationsService.getDashboard());
    }
}
