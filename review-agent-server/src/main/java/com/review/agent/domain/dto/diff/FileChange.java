package com.review.agent.domain.dto.diff;

import com.review.agent.domain.enums.ChangeType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileChange {
    private final String filePath;
    private final ChangeType changeType;
    private final String oldFilePath;
    private final List<Hunk> hunks;
    private final int additions;
    private final int deletions;
}
