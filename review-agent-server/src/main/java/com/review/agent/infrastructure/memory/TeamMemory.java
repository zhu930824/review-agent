package com.review.agent.infrastructure.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemory {
    private String memoryKey;
    private String category;
    private String title;
    private String content;
    private List<String> tags;
    private String source;
    private String severity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
