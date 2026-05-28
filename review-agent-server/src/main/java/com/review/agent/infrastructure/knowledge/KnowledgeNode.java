package com.review.agent.infrastructure.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeNode {
    private String id;
    private String type;
    private String title;
    private String content;
    private String severity;
    private String category;
    private List<String> tags;
    private Map<String, Object> metadata;
}
