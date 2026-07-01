package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.service.IntegrationActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/actions")
@RequiredArgsConstructor
public class IntegrationActionLogController {

    private final IntegrationActionLogService integrationActionLogService;

    @GetMapping
    public Result<List<IntegrationActionLogVO>> listActions(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.success(integrationActionLogService.listRecent(limit));
    }
}
