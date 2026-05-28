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
public class SopDocument {
    private String title;
    private String version;
    private String team;
    private List<SopSection> sections;
}
