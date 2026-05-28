package com.review.agent.infrastructure.refactor;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.memory.TeamMemory;
import com.review.agent.infrastructure.memory.TeamMemoryStore;
import com.review.agent.infrastructure.rule.RuleDefinition;
import com.review.agent.infrastructure.rule.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefactorPlannerService {

    private final RuleRepository ruleRepository;
    private final TeamMemoryStore memoryStore;

    public RefactorPlan generatePlan(List<ReviewFinding> findings) {
        Map<String, List<ReviewFinding>> byFile = findings.stream()
                .filter(f -> f.getFilePath() != null)
                .collect(Collectors.groupingBy(ReviewFinding::getFilePath));

        List<RefactorStep> steps = new ArrayList<>();
        for (Map.Entry<String, List<ReviewFinding>> entry : byFile.entrySet()) {
            steps.add(generateStep(entry.getKey(), entry.getValue()));
        }

        Set<String> categories = findings.stream()
                .filter(f -> f.getCategory() != null)
                .map(f -> f.getCategory().name())
                .collect(Collectors.toSet());

        List<String> relevantMemories = memoryStore.listAll().stream()
                .filter(m -> categories.stream().anyMatch(c -> c.equalsIgnoreCase(m.getCategory())))
                .map(TeamMemory::getTitle)
                .collect(Collectors.toList());

        String dominantCategory = findings.stream()
                .filter(f -> f.getCategory() != null)
                .collect(Collectors.groupingBy(f -> f.getCategory().name(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("OTHER");

        return RefactorPlan.builder()
                .planId(UUID.randomUUID().toString().substring(0, 8))
                .title(dominantCategory + " 技术债治理计划")
                .summary(String.format("基于 %d 个文件中的 %d 个发现问题生成", byFile.size(), findings.size()))
                .totalSteps(steps.size())
                .estimatedImpact(steps.stream().filter(s -> s.isBreaking()).toArray().length * 8 + steps.size() * 3)
                .steps(steps)
                .prerequisites(relevantMemories)
                .risks(List.of("接口变更可能影响下游调用方", "数据库迁移需要灰度验证"))
                .build();
    }

    private RefactorStep generateStep(String filePath, List<ReviewFinding> findings) {
        boolean hasBlocker = findings.stream().anyMatch(f -> f.getSeverity() == Severity.BLOCKER);
        boolean hasSecurity = findings.stream().anyMatch(f -> f.getCategory() == FindingCategory.SECURITY);

        return RefactorStep.builder()
                .stepName("重构 " + filePath)
                .targetFile(filePath)
                .description(String.format("该文件存在 %d 个问题：%s", findings.size(),
                        findings.stream().map(ReviewFinding::getTitle).collect(Collectors.joining(", "))))
                .approach(hasSecurity ? "安全加固 + 代码重构" : "逻辑重构 + 单元测试补充")
                .priority(hasBlocker ? "P0" : hasSecurity ? "P1" : "P2")
                .breaking(hasBlocker)
                .estimatedEffort(hasBlocker ? "3-5 人天" : "1-2 人天")
                .build();
    }
}
