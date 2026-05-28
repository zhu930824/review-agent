package com.review.agent.infrastructure.agent.role;

import com.review.agent.infrastructure.agent.AgentExecutionResult;
import com.review.agent.infrastructure.knowledge.ArchitectureAdvice;
import com.review.agent.infrastructure.knowledge.ArchitectureBrain;
import com.review.agent.infrastructure.knowledge.KnowledgeGraphEngine;
import com.review.agent.infrastructure.memory.TeamMemory;
import com.review.agent.infrastructure.memory.TeamMemoryStore;
import com.review.agent.infrastructure.rule.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArchitectureCommitteeOrchestrator {

    private final ArchitectureBrain architectureBrain;
    private final KnowledgeGraphEngine graphEngine;
    private final RuleRepository ruleRepository;
    private final TeamMemoryStore memoryStore;

    public List<CommitteeMember> assemble() {
        List<CommitteeMember> members = new ArrayList<>();

        List<TeamMemory> architectureMemories = memoryStore.listByCategory("ARCHITECTURE");
        members.add(CommitteeMember.builder()
                .role("ARCHITECT")
                .name("架构师 Agent")
                .expertise("分层架构、DDD、依赖管理")
                .referenceCount(architectureMemories.size())
                .build());

        List<TeamMemory> securityMemories = memoryStore.listByCategory("SECURITY");
        members.add(CommitteeMember.builder()
                .role("SECURITY")
                .name("安全 Agent")
                .expertise("注入防护、敏感信息保护、序列化安全")
                .referenceCount(securityMemories.size())
                .build());

        List<TeamMemory> perfMemories = memoryStore.listByCategory("PERFORMANCE");
        members.add(CommitteeMember.builder()
                .role("PERFORMANCE")
                .name("性能 Agent")
                .expertise("N+1 查询、缓存策略、GC 优化")
                .referenceCount(perfMemories.size())
                .build());

        members.add(CommitteeMember.builder()
                .role("DBA")
                .name("DBA Agent")
                .expertise("索引设计、SQL 优化、数据一致性")
                .referenceCount(0)
                .build());

        return members;
    }

    public CommitteeVerdict review(List<String> changedFiles, ArchitectureAdvice advice) {
        List<CommitteeMember> members = assemble();

        int total = members.size();
        long approve = 0;
        long reject = 0;
        List<String> votes = new ArrayList<>();
        List<String> concerns = new ArrayList<>();

        for (CommitteeMember member : members) {
            String vote;
            if (advice.getHighRiskItems() != null && !advice.getHighRiskItems().isEmpty()) {
                vote = "REJECT";
                reject++;
            } else if (member.getReferenceCount() > 0) {
                vote = "APPROVE";
                approve++;
            } else {
                vote = "ABSTAIN";
            }
            votes.add(member.getRole() + ": " + vote);
        }

        if (advice.getPatterns() != null) {
            concerns.addAll(advice.getPatterns());
        }

        String decision;
        if (reject > total / 2) decision = "REJECTED";
        else if (approve >= reject && approve > 0) decision = "APPROVED";
        else decision = "NEEDS_DISCUSSION";

        return CommitteeVerdict.builder()
                .decision(decision)
                .votes(votes)
                .concerns(concerns)
                .recommendations(advice.getRecommendations())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommitteeMember {
        private String role;
        private String name;
        private String expertise;
        private int referenceCount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommitteeVerdict {
        private String decision;
        private List<String> votes;
        private List<String> concerns;
        private List<String> recommendations;
    }
}
