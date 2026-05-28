package com.review.agent.infrastructure.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraph {
    private List<KnowledgeNode> nodes;
    private List<KnowledgeEdge> edges;
}
