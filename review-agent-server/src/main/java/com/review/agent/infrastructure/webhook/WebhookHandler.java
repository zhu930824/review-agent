package com.review.agent.infrastructure.webhook;

public interface WebhookHandler {

    String source();

    WebhookEvent parse(String payload, String signature);

    boolean verifySignature(String payload, String signature);
}
