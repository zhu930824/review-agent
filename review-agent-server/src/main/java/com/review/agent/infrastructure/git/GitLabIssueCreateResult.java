package com.review.agent.infrastructure.git;

import java.time.LocalDateTime;

public record GitLabIssueCreateResult(
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

    public GitLabIssueCreateResult(String id, String iid, String webUrl, String requestUrl) {
        this(id, iid, webUrl, null, null, null, null, null, null, null, requestUrl);
    }
}
