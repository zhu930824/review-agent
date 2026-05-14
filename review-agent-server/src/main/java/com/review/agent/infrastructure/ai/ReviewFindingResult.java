package com.review.agent.infrastructure.ai;

import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.Severity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewFindingResult {
    private FindingCategory category;
    private Severity severity;
    private String title;
    private String description;
    private String suggestion;
    private Integer lineStart;
    private Integer lineEnd;
    private String modelName;
    private String filePath;
    private boolean crossHit;
    private double confidence;
}
