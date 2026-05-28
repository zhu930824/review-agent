package com.review.agent.infrastructure.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubWebhookHandler implements WebhookHandler {

    private final ObjectMapper objectMapper;

    @Override
    public String source() {
        return "github";
    }

    @Override
    public WebhookEvent parse(String payload, String signature) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            WebhookEvent event = new WebhookEvent();
            event.setSource("github");

            String action = root.path("action").asText();
            boolean isPr = root.has("pull_request");
            if (isPr) {
                event.setEventType("pull_request." + action);
                JsonNode pr = root.path("pull_request");
                event.setPrTitle(pr.path("title").asText());
                event.setSourceBranch(pr.path("head").path("ref").asText());
                event.setTargetBranch(pr.path("base").path("ref").asText());
                event.setRepositoryUrl(pr.path("base").path("repo").path("html_url").asText());
                event.setProjectKey(pr.path("base").path("repo").path("full_name").asText());
            } else {
                event.setEventType(root.path("zen").asText("ping"));
            }
            event.setRawPayload(payload);
            return event;
        } catch (Exception e) {
            log.error("解析 GitHub webhook 失败", e);
            return null;
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        return true;
    }
}
