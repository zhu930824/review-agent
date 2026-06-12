package com.review.agent.infrastructure.ci;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateGitHubSarifUploadClient implements GitHubSarifUploadClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void upload(GitHubSarifUploadRequest request) {
        HttpHeaders headers = new HttpHeaders();
        request.headers().forEach(headers::set);
        restTemplate.postForEntity(request.url(), new HttpEntity<>(request.body(), headers), String.class);
    }
}
