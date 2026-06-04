package com.review.agent.infrastructure.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class HttpPrePrReportPublisher implements PrePrReportPublisher {

    private final boolean enabled;
    private final String endpoint;
    private final String token;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpPrePrReportPublisher(
            @Value("${review-agent.pre-pr-report-publish.enabled:false}") boolean enabled,
            @Value("${review-agent.pre-pr-report-publish.endpoint:}") String endpoint,
            @Value("${review-agent.pre-pr-report-publish.token:}") String token) {
        this(enabled, endpoint, token, HttpClient.newHttpClient(), new ObjectMapper());
    }

    HttpPrePrReportPublisher(
            boolean enabled,
            String endpoint,
            String token,
            HttpClient httpClient,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.endpoint = endpoint == null ? "" : endpoint;
        this.token = token == null ? "" : token;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean publish(Long reviewId, String markdown) {
        if (!enabled || endpoint.isBlank()) {
            log.debug("[PrePR-Report] external publish disabled for review={}", reviewId);
            return false;
        }

        try {
            PrePrReportPublishPayload payload = new PrePrReportPublishPayload(reviewId, "MARKDOWN", markdown == null ? "" : markdown);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));

            if (!token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("[PrePR-Report] external publish failed review={} status={} body={}",
                        reviewId, response.statusCode(), response.body());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("[PrePR-Report] external publish failed review={}", reviewId, e);
            return false;
        }
    }
}
