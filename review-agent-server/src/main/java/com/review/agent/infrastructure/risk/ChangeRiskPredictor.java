package com.review.agent.infrastructure.risk;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.knowledge.KnowledgeGraphEngine;
import com.review.agent.infrastructure.knowledge.KnowledgeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeRiskPredictor {

    private final KnowledgeGraphEngine graphEngine;

    public RiskAssessment predict(List<ReviewFinding> findings, List<String> changedFiles) {
        int score = 0;
        List<String> factors = new ArrayList<>();
        List<String> fileRisks = new ArrayList<>();

        if (changedFiles != null && changedFiles.size() > 10) {
            score += 20;
            factors.add("变更文件数 > 10（" + changedFiles.size() + " 个文件）");
        }

        long blockerCount = findings.stream()
                .filter(f -> f.getSeverity() != null && f.getSeverity().name().equals("BLOCKER"))
                .count();
        if (blockerCount > 0) {
            score += 30;
            factors.add("存在 " + blockerCount + " 个 BLOCKER 级别问题");
        }

        List<KnowledgeNode> relatedNodes = graphEngine.query(
                findings.stream()
                        .filter(f -> f.getFilePath() != null)
                        .map(ReviewFinding::getFilePath)
                        .collect(Collectors.joining(" ")));
        if (relatedNodes.size() > 5) {
            score += 15;
            factors.add("命中知识图谱 " + relatedNodes.size() + " 个关联节点");
        }

        Map<String, Long> fileFindingCount = findings.stream()
                .filter(f -> f.getFilePath() != null)
                .collect(Collectors.groupingBy(ReviewFinding::getFilePath, Collectors.counting()));
        fileFindingCount.forEach((file, count) -> {
            if (count > 3) {
                fileRisks.add(file + " (" + count + " 个问题)");
            }
        });

        String level;
        if (score >= 60) level = "CRITICAL";
        else if (score >= 30) level = "HIGH";
        else if (score >= 15) level = "MEDIUM";
        else level = "LOW";

        return RiskAssessment.builder()
                .level(level)
                .score(score)
                .factors(factors)
                .fileLevelRisks(fileRisks)
                .recommendation(buildRecommendation(level))
                .build();
    }

    private String buildRecommendation(String level) {
        return switch (level) {
            case "CRITICAL" -> "强烈建议阻断合并，先修复所有 BLOCKER 问题再进行 PR";
            case "HIGH" -> "建议增加额外 Review，重点检查高风险文件";
            case "MEDIUM" -> "常规审查流程，关注文件级风险点";
            default -> "低风险变更，可走快速审查通道";
        };
    }
}
