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
public class ArchitectureAdvice {
    private String summary;
    private String dominantCategory;
    private List<String> highRiskItems;
    private List<String> patterns;
    private List<String> recommendations;
    private long rulesCount;
    private long memoriesCount;
}
