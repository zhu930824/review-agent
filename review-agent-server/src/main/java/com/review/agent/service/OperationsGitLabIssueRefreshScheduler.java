package com.review.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationsGitLabIssueRefreshScheduler {

    private final OperationsTaskService taskService;

    @Value("${review-agent.operations.gitlab-issue-refresh-limit:20}")
    private int refreshLimit;

    @Scheduled(fixedDelayString = "${review-agent.operations.gitlab-issue-refresh-delay-ms:300000}")
    public void refreshRecentGitLabIssues() {
        taskService.refreshRecentGitLabIssues(refreshLimit);
    }
}
