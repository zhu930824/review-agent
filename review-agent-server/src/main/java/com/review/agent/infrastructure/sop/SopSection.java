package com.review.agent.infrastructure.sop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SopSection {
    private String title;
    private String category;
    private String description;
    private String defaultSeverity;
    private String violationMessage;
    private String fixSuggestion;
    private List<String> checkItems;
    private boolean actionable;
}
