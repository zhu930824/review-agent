package com.review.agent.domain.dto.diff;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Hunk {
    private final int oldStart;
    private final int oldLines;
    private final int newStart;
    private final int newLines;
    private final String content;
    private final List<DiffLine> lines;
}
