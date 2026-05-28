package com.review.agent.infrastructure.knowledge;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.memory.TeamMemory;
import com.review.agent.infrastructure.memory.TeamMemoryStore;
import com.review.agent.infrastructure.rule.RuleDefinition;
import com.review.agent.infrastructure.rule.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeGraphEngine {

    private final TeamMemoryStore memoryStore;
    private final RuleRepository ruleRepository;

    public KnowledgeGraph buildFullGraph(List<ReviewFinding> recentFindings) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        List<KnowledgeEdge> edges = new ArrayList<>();

        List<TeamMemory> memories = memoryStore.listAll();
        List<RuleDefinition> rules = ruleRepository.listEnabled();

        for (TeamMemory m : memories) {
            nodes.add(KnowledgeNode.builder()
                    .id("memory:" + m.getMemoryKey())
                    .type("MEMORY")
                    .title(m.getTitle())
                    .content(m.getContent())
                    .severity(m.getSeverity())
                    .category(m.getCategory())
                    .tags(m.getTags())
                    .metadata(Map.of("source", m.getSource() != null ? m.getSource() : ""))
                    .build());
        }

        for (RuleDefinition r : rules) {
            nodes.add(KnowledgeNode.builder()
                    .id("rule:" + r.getRuleKey())
                    .type("RULE")
                    .title(r.getName())
                    .content(r.getDescription())
                    .severity(r.getSeverity())
                    .category(r.getCategory())
                    .metadata(Map.of("enabled", String.valueOf(!Boolean.FALSE.equals(r.getEnabled()))))
                    .build());
        }

        if (recentFindings != null) {
            for (ReviewFinding f : recentFindings) {
                String id = "finding:" + f.getId();
                nodes.add(KnowledgeNode.builder()
                        .id(id)
                        .type("FINDING")
                        .title(f.getTitle())
                        .content(f.getDescription())
                        .severity(f.getSeverity() != null ? f.getSeverity().name() : null)
                        .category(f.getCategory() != null ? f.getCategory().name() : null)
                        .metadata(Map.of("filePath", f.getFilePath() != null ? f.getFilePath() : ""))
                        .build());
            }
        }

        for (TeamMemory m : memories) {
            for (RuleDefinition r : rules) {
                if (matchesMemoryToRule(m, r)) {
                    edges.add(KnowledgeEdge.builder()
                            .from("memory:" + m.getMemoryKey())
                            .to("rule:" + r.getRuleKey())
                            .type("DERIVES")
                            .label("经验沉淀为规则")
                            .build());
                }
            }
        }

        for (RuleDefinition a : rules) {
            for (RuleDefinition b : rules) {
                if (!a.getRuleKey().equals(b.getRuleKey()) && isRelated(a, b)) {
                    edges.add(KnowledgeEdge.builder()
                            .from("rule:" + a.getRuleKey())
                            .to("rule:" + b.getRuleKey())
                            .type("RELATES_TO")
                            .label("规则关联")
                            .build());
                }
            }
        }

        log.info("知识图谱构建完成: {} 节点, {} 边", nodes.size(), edges.size());
        return KnowledgeGraph.builder().nodes(nodes).edges(edges).build();
    }

    public List<KnowledgeNode> query(String keyword) {
        KnowledgeGraph graph = buildFullGraph(null);
        if (keyword == null || keyword.isBlank()) {
            return graph.getNodes();
        }
        String lower = keyword.toLowerCase();
        return graph.getNodes().stream()
                .filter(n -> (n.getTitle() != null && n.getTitle().toLowerCase().contains(lower))
                        || (n.getContent() != null && n.getContent().toLowerCase().contains(lower))
                        || (n.getTags() != null && n.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lower))))
                .collect(Collectors.toList());
    }

    public List<KnowledgeNode> findRelated(String nodeId) {
        KnowledgeGraph graph = buildFullGraph(null);
        Set<String> relatedIds = new HashSet<>();

        for (KnowledgeEdge e : graph.getEdges()) {
            if (e.getFrom().equals(nodeId)) relatedIds.add(e.getTo());
            if (e.getTo().equals(nodeId)) relatedIds.add(e.getFrom());
        }

        return graph.getNodes().stream()
                .filter(n -> relatedIds.contains(n.getId()))
                .collect(Collectors.toList());
    }

    private boolean matchesMemoryToRule(TeamMemory memory, RuleDefinition rule) {
        if (memory.getCategory() != null && memory.getCategory().equalsIgnoreCase(rule.getCategory())) {
            return true;
        }
        if (memory.getTags() != null && rule.getCategory() != null) {
            return memory.getTags().stream()
                    .anyMatch(t -> t.toLowerCase().contains(rule.getCategory().toLowerCase()));
        }
        return false;
    }

    private boolean isRelated(RuleDefinition a, RuleDefinition b) {
        return a.getCategory() != null && a.getCategory().equalsIgnoreCase(b.getCategory());
    }
}
