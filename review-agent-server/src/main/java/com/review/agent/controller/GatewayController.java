package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.gateway.AiGateway;
import com.review.agent.infrastructure.gateway.ModelCallRecord;
import com.review.agent.infrastructure.gateway.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final AiGateway aiGateway;

    @GetMapping("/prompts")
    public Result<List<PromptTemplate>> listPrompts() {
        return Result.success(aiGateway.listPrompts());
    }

    @GetMapping("/prompts/{key}")
    public Result<PromptTemplate> getPrompt(@PathVariable("key") String key) {
        PromptTemplate prompt = aiGateway.getPrompt(key);
        if (prompt == null) {
            return Result.fail(404, "Prompt 模板不存在: " + key);
        }
        return Result.success(prompt);
    }

    @PostMapping("/prompts/resolve")
    public Result<PromptTemplate> resolvePrompt(@RequestBody Map<String, Object> request) {
        String key = (String) request.get("templateKey");
        @SuppressWarnings("unchecked")
        Map<String, String> vars = (Map<String, String>) request.getOrDefault("variables", Map.of());
        PromptTemplate resolved = aiGateway.resolvePrompt(key, vars);
        if (resolved == null) {
            return Result.fail(404, "Prompt 模板不存在: " + key);
        }
        return Result.success(resolved);
    }

    @GetMapping("/audit")
    public Result<List<ModelCallRecord>> getAuditLog(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return Result.success(aiGateway.getRecentCalls(status, limit));
    }

    @GetMapping("/stats")
    public Result<AiGateway.ModelCallStats> getStats() {
        return Result.success(aiGateway.getStats());
    }
}
