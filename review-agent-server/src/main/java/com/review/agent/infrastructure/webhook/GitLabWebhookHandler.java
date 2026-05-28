package com.review.agent.infrastructure.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitLabWebhookHandler implements WebhookHandler {

    private final ObjectMapper objectMapper;

    @Override
    public String source() {
        return "gitlab";
    }

    @Override
    public WebhookEvent parse(String payload, String signature) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            WebhookEvent event = new WebhookEvent();
            event.setSource("gitlab");

            String objectKind = root.path("object_kind").asText();
            if ("merge_request".equals(objectKind)) {
                JsonNode attrs = root.path("object_attributes");
                event.setEventType("merge_request." + attrs.path("action").asText());
                event.setPrTitle(attrs.path("title").asText());
                event.setSourceBranch(attrs.path("source_branch").asText());
                event.setTargetBranch(attrs.path("target_branch").asText());
                event.setProjectKey(root.path("project").path("path_with_namespace").asText());
                event.setRepositoryUrl(attrs.path("source").path("http_url").asText());
            } else {
                event.setEventType(objectKind);
            }
            event.setRawPayload(payload);
            return event;
        } catch (Exception e) {
            log.error("解析 GitLab webhook 失败", e);
            return null;
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        return true;
    }
}
