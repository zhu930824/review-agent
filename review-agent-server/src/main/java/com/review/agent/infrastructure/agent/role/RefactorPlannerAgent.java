package com.review.agent.infrastructure.agent.role;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.knowledge.ArchitectureBrain;
import com.review.agent.infrastructure.knowledge.ArchitectureAdvice;
import com.review.agent.infrastructure.memory.TeamMemoryStore;
import com.review.agent.infrastructure.refactor.RefactorPlan;
import com.review.agent.infrastructure.refactor.RefactorPlannerService;
import com.review.agent.infrastructure.rule.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefactorPlannerAgent {

    private final RefactorPlannerService refactorPlanner;
    private final ArchitectureBrain architectureBrain;
    private final TeamMemoryStore memoryStore;
    private final RuleRepository ruleRepository;

    public RefactorPlan planWithContext(List<ReviewFinding> findings) {
        RefactorPlan basePlan = refactorPlanner.generatePlan(findings);

        ArchitectureAdvice advice = architectureBrain.analyze(findings);

        List<String> enrichedPrerequisites = new java.util.ArrayList<>(basePlan.getPrerequisites());
        enrichedPrerequisites.add("架构建议: " + advice.getSummary());
        if (advice.getRecommendations() != null) {
            enrichedPrerequisites.addAll(advice.getRecommendations());
        }

        long memoriesCount = memoryStore.listByCategory(advice.getDominantCategory()).size();
        long rulesCount = ruleRepository.listByCategory(advice.getDominantCategory()).size();

        return RefactorPlan.builder()
                .planId(basePlan.getPlanId())
                .title(basePlan.getTitle() + "（AI 增强）")
                .summary(basePlan.getSummary()
                        + " | 知识库: " + memoriesCount + " 条记忆, " + rulesCount + " 条规则")
                .totalSteps(basePlan.getTotalSteps())
                .estimatedImpact(basePlan.getEstimatedImpact())
                .steps(basePlan.getSteps())
                .prerequisites(enrichedPrerequisites)
                .risks(basePlan.getRisks())
                .build();
    }
}
