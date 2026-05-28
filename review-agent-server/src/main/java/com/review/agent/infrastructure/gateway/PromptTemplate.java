package com.review.agent.infrastructure.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    private String templateKey;
    private String name;
    private String category;
    private String content;
    private String variables;
    private String version;
    private LocalDateTime createdAt;
}
