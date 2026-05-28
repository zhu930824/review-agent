package com.review.agent.infrastructure.knowledge;

import com.review.agent.domain.entity.ReviewFinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchitectureBrain {

    private final KnowledgeGraphEngine graphEngine;

    public ArchitectureAdvice analyze(List<ReviewFinding> findings) {
        KnowledgeGraph graph = graphEngine.buildFullGraph(findings);

        Map<String, Long> categoryCount = findings.stream()
                .filter(f -> f.getCategory() != null)
                .collect(Collectors.groupingBy(f -> f.getCategory().name(), Collectors.counting()));

        Map<String, Long> severityCount = findings.stream()
                .filter(f -> f.getSeverity() != null)
                .collect(Collectors.groupingBy(f -> f.getSeverity().name(), Collectors.counting()));

        List<String> highRisk = new ArrayList<>();
        if (severityCount.getOrDefault("BLOCKER", 0L) > 0) {
            highRisk.add("存在 BLOCKER 级别发现问题，建议阻断合并");
        }

        String dominantCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NONE");

        List<String> patterns = analyzePatterns(graph, findings);
        List<String> recommendations = generateRecommendations(graph, dominantCategory, patterns);

        return ArchitectureAdvice.builder()
                .summary(String.format("共发现 %d 个问题，主要集中在 %s 领域", findings.size(), dominantCategory))
                .dominantCategory(dominantCategory)
                .highRiskItems(highRisk)
                .patterns(patterns)
                .recommendations(recommendations)
                .rulesCount(graph.getNodes().stream().filter(n -> "RULE".equals(n.getType())).count())
                .memoriesCount(graph.getNodes().stream().filter(n -> "MEMORY".equals(n.getType())).count())
                .build();
    }

    private List<String> analyzePatterns(KnowledgeGraph graph, List<ReviewFinding> findings) {
        List<String> patterns = new ArrayList<>();
        if (findings == null || findings.isEmpty()) return patterns;

        Set<String> affectedDirs = findings.stream()
                .map(f -> {
                    String path = f.getFilePath();
                    if (path == null) return "/";
                    int idx = path.lastIndexOf('/');
                    return idx > 0 ? path.substring(0, idx) : "/";
                })
                .collect(Collectors.toSet());

        if (affectedDirs.size() > 3) {
            patterns.add("问题分散在多个模块，建议按模块分批治理");
        }
        if (graph.getEdges().size() > 5) {
            patterns.add("多条规则之间存在关联，建议建立系统化治理机制");
        }
        return patterns;
    }

    private List<String> generateRecommendations(KnowledgeGraph graph, String dominantCategory, List<String> patterns) {
        List<String> recs = new ArrayList<>();

        switch (dominantCategory) {
            case "SECURITY" -> {
                recs.add("安全类问题占比最高，建议启动专项安全审计");
                recs.add("将安全规则纳入 CI 阻断策略，从源头拦截");
            }
            case "PERFORMANCE" -> {
                recs.add("性能问题集中，建议建立性能基线测试");
                recs.add("将性能规则提升为 BLOCKER 级别");
            }
            case "CODE_STYLE" -> {
                recs.add("代码规范问题可通过 IDE 插件和 Pre-Commit Hook 自动修复");
                recs.add("建议统一 IDE 代码模板和格式化配置");
            }
            case "ARCHITECTURE" -> {
                recs.add("架构违规需要开发团队同步对齐分层原则");
                recs.add("建议组织一次架构对齐评审会议");
            }
            default -> recs.add("建议逐项 Review，优先处理 BLOCKER 和 MAJOR 级别问题");
        }
        return recs;
    }
}
