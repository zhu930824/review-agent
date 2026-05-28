package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.workflow.WorkflowEngine;
import com.review.agent.infrastructure.workflow.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngine workflowEngine;

    @PostMapping
    public Result<String> startWorkflow(@RequestBody Map<String, Object> request) {
        String workflowKey = (String) request.getOrDefault("workflowKey", "default");
        @SuppressWarnings("unchecked")
        Map<String, Object> inputs = (Map<String, Object>) request.getOrDefault("inputs", Map.of());
        String workflowId = workflowEngine.startWorkflow(workflowKey, inputs);
        return Result.success(workflowId);
    }

    @GetMapping("/{workflowId}")
    public Result<WorkflowStatus> getWorkflowStatus(@PathVariable("workflowId") String workflowId) {
        WorkflowStatus status = workflowEngine.getStatus(workflowId);
        if (status == null) {
            return Result.fail(404, "工作流实例不存在: " + workflowId);
        }
        return Result.success(status);
    }
}
