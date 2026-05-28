package com.review.agent.infrastructure.integration;

public interface IssueTracker {

    String source();

    String createIssue(String title, String description, String severity, String assignee);
}
