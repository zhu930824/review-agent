package com.review.agent.infrastructure.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JenkinsWebhookHandler implements WebhookHandler {

    private final ObjectMapper objectMapper;

    @Override
    public String source() {
        return "jenkins";
    }

    @Override
    public WebhookEvent parse(String payload, String signature) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            WebhookEvent event = new WebhookEvent();
            event.setSource("jenkins");

            String phase = root.path("phase").asText();
            String status = root.path("status").asText();
            event.setEventType("pipeline." + phase + "." + status);

            if (root.has("parameters")) {
                JsonNode params = root.path("parameters");
                event.setProjectKey(params.path("PROJECT_KEY").asText(""));
                event.setSourceBranch(params.path("SOURCE_BRANCH").asText(""));
                event.setTargetBranch(params.path("TARGET_BRANCH").asText(""));
                event.setRepositoryUrl(params.path("REPO_URL").asText(""));
            }

            if (root.has("displayName")) {
                event.setPrTitle(root.path("displayName").asText());
            }

            event.setRawPayload(payload);
            return event;
        } catch (Exception e) {
            log.error("解析 Jenkins webhook 失败", e);
            return null;
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        return true;
    }
}
