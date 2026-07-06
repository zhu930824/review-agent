package com.review.agent.infrastructure.git;

import java.time.LocalDateTime;

public record GitLabIssueStateResult(
        String id,
        String iid,
        String webUrl,
        String state,
        String title,
        String labels,
        String assigneeUsername,
        String authorUsername,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        String requestUrl) {

    public GitLabIssueStateResult(String id, String iid, String webUrl, String state, String requestUrl) {
        this(id, iid, webUrl, state, null, null, null, null, null, null, requestUrl);
    }
}
