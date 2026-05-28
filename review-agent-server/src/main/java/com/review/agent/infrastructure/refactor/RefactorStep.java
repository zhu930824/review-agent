package com.review.agent.infrastructure.refactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefactorStep {
    private String stepName;
    private String targetFile;
    private String description;
    private String approach;
    private String priority;
    private boolean breaking;
    private String estimatedEffort;
}
