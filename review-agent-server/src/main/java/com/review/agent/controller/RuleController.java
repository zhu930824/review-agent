package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.rule.RuleDefinition;
import com.review.agent.infrastructure.rule.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleRepository ruleRepository;

    @GetMapping
    public Result<List<RuleDefinition>> listRules(
            @RequestParam(value = "category", required = false) String category) {
        if (category != null) {
            return Result.success(ruleRepository.listByCategory(category));
        }
        return Result.success(ruleRepository.listEnabled());
    }

    @GetMapping("/{ruleKey}")
    public Result<RuleDefinition> getRule(@PathVariable("ruleKey") String ruleKey) {
        RuleDefinition rule = ruleRepository.get(ruleKey);
        if (rule == null) {
            return Result.fail(404, "规则不存在: " + ruleKey);
        }
        return Result.success(rule);
    }
}
