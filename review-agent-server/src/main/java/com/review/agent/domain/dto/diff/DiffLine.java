package com.review.agent.domain.dto.diff;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiffLine {
    private final LineType type;
    private final String content;
    private final int lineNumber;
}
