package com.review.agent.infrastructure.fix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixDraft {
    private String filePath;
    private Integer lineStart;
    private Integer lineEnd;
    private String originalContent;
    private String fixedContent;
    private String explanation;
    private String confidence;
    private Boolean needsHumanReview;
}
