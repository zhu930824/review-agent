package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.rule.RuleDefinition;
import com.review.agent.infrastructure.rule.RuleRepository;
import com.review.agent.infrastructure.rule.SopCompiler;
import com.review.agent.infrastructure.sop.SopDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sop")
@RequiredArgsConstructor
public class SopController {

    private final SopCompiler sopCompiler;
    private final RuleRepository ruleRepository;

    @PostMapping("/compile")
    public Result<List<RuleDefinition>> compileSop(@RequestBody SopDocument document) {
        List<RuleDefinition> rules = sopCompiler.compile(document);
        rules.forEach(ruleRepository::register);
        return Result.success(rules);
    }
}
