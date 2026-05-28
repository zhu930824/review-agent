package com.review.agent.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraConnector implements IssueTracker {

    @Override
    public String source() {
        return "jira";
    }

    @Override
    public String createIssue(String title, String description, String severity, String assignee) {
        log.info("[JIRA] 创建工单: title={}, severity={}, assignee={}", title, severity, assignee);
        return "JIRA-" + System.currentTimeMillis() % 100000;
    }
}
