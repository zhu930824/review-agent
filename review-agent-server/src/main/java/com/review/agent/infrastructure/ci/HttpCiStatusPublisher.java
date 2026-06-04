package com.review.agent.infrastructure.ci;

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
public class HttpCiStatusPublisher {

    private final boolean enabled;
    private final String endpoint;
    private final String token;
    private final String provider;
    private final String apiBaseUrl;
    private final CiProviderPayloadFactory providerPayloadFactory;
    private final CiStatusEndpointFactory endpointFactory;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpCiStatusPublisher(
            @Value("${review-agent.ci-status.enabled:false}") boolean enabled,
            @Value("${review-agent.ci-status.endpoint:}") String endpoint,
            @Value("${review-agent.ci-status.token:}") String token,
            @Value("${review-agent.ci-status.provider:generic}") String provider,
            @Value("${review-agent.ci-status.api-base-url:}") String apiBaseUrl,
            CiProviderPayloadFactory providerPayloadFactory) {
        this(enabled, endpoint, token, provider, apiBaseUrl, providerPayloadFactory, HttpClient.newHttpClient(), new ObjectMapper());
    }

    HttpCiStatusPublisher(
            boolean enabled,
            String endpoint,
            String token,
            String provider,
            CiProviderPayloadFactory providerPayloadFactory) {
        this(enabled, endpoint, token, provider, "", providerPayloadFactory, HttpClient.newHttpClient(), new ObjectMapper());
    }

    HttpCiStatusPublisher(
            boolean enabled,
            String endpoint,
            String token,
            String provider,
            String apiBaseUrl,
            CiProviderPayloadFactory providerPayloadFactory,
            HttpClient httpClient,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.endpoint = endpoint == null ? "" : endpoint;
        this.token = token == null ? "" : token;
        this.provider = provider == null || provider.isBlank() ? "generic" : provider.toLowerCase();
        this.apiBaseUrl = apiBaseUrl == null ? "" : apiBaseUrl;
        this.providerPayloadFactory = providerPayloadFactory;
        this.endpointFactory = new CiStatusEndpointFactory(this.apiBaseUrl);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public void publish(CiStatusPayload payload) {
        String publishEndpoint = resolveEndpoint(payload);
        if (!enabled || publishEndpoint.isBlank()) {
            log.debug("[CI-Status] external publish disabled for review={}", payload.reviewId());
            return;
        }

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(publishEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(toProviderPayload(payload))));

            if (!token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("[CI-Status] external publish failed review={} status={} body={}",
                        payload.reviewId(), response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("[CI-Status] external publish failed review={}", payload.reviewId(), e);
        }
    }

    private String resolveEndpoint(CiStatusPayload payload) {
        if (apiBaseUrl.isBlank() || payload.repoUrl().isBlank() || payload.statusSha().isBlank()) {
            return endpoint;
        }
        return switch (provider) {
            case "github" -> endpointFactory.github(payload.repoUrl(), payload.statusSha()).url();
            case "gitlab" -> endpointFactory.gitlab(payload.repoUrl(), payload.statusSha()).url();
            default -> endpointFactory.generic(endpoint).url();
        };
    }

    private Object toProviderPayload(CiStatusPayload payload) {
        return switch (provider) {
            case "github" -> providerPayloadFactory.toGitHub(payload);
            case "gitlab" -> providerPayloadFactory.toGitLab(payload);
            default -> payload;
        };
    }
}
