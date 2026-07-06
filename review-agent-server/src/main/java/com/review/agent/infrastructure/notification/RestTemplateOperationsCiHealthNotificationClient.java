package com.review.agent.infrastructure.notification;

import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestTemplateOperationsCiHealthNotificationClient implements OperationsCiHealthNotificationClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String webhookUrl, OperationsCiHealthActionVO action) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(webhookUrl, new HttpEntity<>(payload(action), headers), String.class);
    }

    private Map<String, Object> payload(OperationsCiHealthActionVO action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "CI_HEALTH_ACTION");
        payload.put("priority", action.getNotificationPriority());
        payload.put("dedupKey", action.getNotificationDedupKey());
        payload.put("title", action.getNotificationTitle());
        payload.put("body", action.getNotificationBody());
        payload.put("targetUrl", action.getNotificationTargetUrl());
        payload.put("connectorKey", action.getConnectorKey());
        payload.put("provider", action.getProvider());
        payload.put("healthStatus", action.getHealthStatus());
        payload.put("severity", action.getSeverity());
        payload.put("ownerRole", action.getOwnerRole());
        payload.put("slaHours", action.getSlaHours());
        payload.put("latestSignal", action.getLatestSignal());
        return payload;
    }
}
