package com.review.agent.infrastructure.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEdge {
    private String from;
    private String to;
    private String type;
    private String label;
}
