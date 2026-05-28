package com.review.agent.infrastructure.webhook;

import lombok.Data;

@Data
public class WebhookEvent {

    private String source;
    private String eventType;
    private String projectKey;
    private String repositoryUrl;
    private String sourceBranch;
    private String targetBranch;
    private String prTitle;
    private String rawPayload;
}
