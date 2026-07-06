package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.ProjectGitLabConfig;

public record OperationsIssueSyncContext(
        String taskKey,
        String sourceType,
        String sourceId,
        String sourceRef,
        String title,
        String severity,
        String ownerRole,
        String latestSignal,
        String recommendation,
        Long reviewId,
        Long projectId,
        String projectName,
        ProjectGitLabConfig gitLabConfig) {
}
