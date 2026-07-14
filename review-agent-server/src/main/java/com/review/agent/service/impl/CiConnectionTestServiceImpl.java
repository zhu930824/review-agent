package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.CiConnectionTestResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.ci.JenkinsGateRequestFactory;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.service.CiConnectionTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CiConnectionTestServiceImpl implements CiConnectionTestService {

    private final CiStatusConfigMapper configMapper;
    private final IntegrationActionLogRepository actionLogRepository;
    private final JenkinsGateRequestFactory jenkinsRequestFactory;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public CiConnectionTestResultVO test(String connectorKey) {
        CiStatusConfig config = configMapper.selectOne(new LambdaQueryWrapper<CiStatusConfig>()
                .eq(CiStatusConfig::getConnectorKey, connectorKey));
        if (config == null) {
            return result(connectorKey, null, "SKIPPED", "Connector configuration not found", null, 0L);
        }

        String requestUrl;
        HttpHeaders headers = new HttpHeaders();
        try {
            requestUrl = request(config, headers);
        } catch (Exception ex) {
            CiConnectionTestResultVO result = result(
                    connectorKey, config.getProvider(), "SKIPPED", safeMessage(ex), null, 0L);
            safeRecord(config, result);
            return result;
        }

        Instant startedAt = Instant.now();
        CiConnectionTestResultVO result;
        try {
            restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            result = result(connectorKey, config.getProvider(), "SUCCESS", "Connection verified",
                    requestUrl, Duration.between(startedAt, Instant.now()).toMillis());
        } catch (Exception ex) {
            result = result(connectorKey, config.getProvider(), "FAILED", safeMessage(ex),
                    requestUrl, Duration.between(startedAt, Instant.now()).toMillis());
        }
        safeRecord(config, result);
        return result;
    }

    private String request(CiStatusConfig config, HttpHeaders headers) {
        if (!hasText(config.getProvider())) {
            throw new IllegalStateException("CI provider is missing");
        }
        if ("JENKINS".equalsIgnoreCase(config.getProvider())) {
            require(config.getRepoUrl(), "Jenkins URL");
            require(config.getRepoName(), "Jenkins Job name");
            require(config.getApiToken(), "Jenkins API token");
            jenkinsRequestFactory.buildCrumbHeaders(config).forEach(headers::set);
            return jenkinsRequestFactory.buildJobApiUrl(config);
        }
        if ("GITLAB".equalsIgnoreCase(config.getProvider())) {
            require(config.getRepoUrl(), "GitLab repository URL");
            require(config.getApiToken(), "GitLab API token");
            headers.set("PRIVATE-TOKEN", config.getApiToken());
            URI repoUri = URI.create(config.getRepoUrl());
            String projectPath = hasText(config.getRepoOwner()) && hasText(config.getRepoName())
                    ? config.getRepoOwner() + "/" + config.getRepoName()
                    : trimGitSuffix(repoUri.getPath().replaceFirst("^/", ""));
            return baseUrl(repoUri) + "/api/v4/projects/"
                    + URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
        }
        if ("GITHUB".equalsIgnoreCase(config.getProvider())) {
            require(config.getRepoOwner(), "GitHub repository owner");
            require(config.getRepoName(), "GitHub repository name");
            require(config.getApiToken(), "GitHub API token");
            headers.setBearerAuth(config.getApiToken());
            headers.set("Accept", "application/vnd.github+json");
            return "https://api.github.com/repos/" + config.getRepoOwner() + "/" + config.getRepoName();
        }
        throw new IllegalArgumentException("Unsupported CI provider: " + config.getProvider());
    }

    private void safeRecord(CiStatusConfig config, CiConnectionTestResultVO result) {
        try {
            LocalDateTime now = LocalDateTime.now();
            IntegrationActionLog logEntry = new IntegrationActionLog();
            logEntry.setConnectorKey(config.getConnectorKey());
            logEntry.setProvider(config.getProvider());
            logEntry.setActionType("CONNECTION_TEST");
            logEntry.setActionStatus(result.getStatus());
            logEntry.setTargetKey(config.getRepoName());
            logEntry.setRequestUrl(result.getRequestUrl());
            logEntry.setErrorMessage("SUCCESS".equals(result.getStatus()) ? null : result.getMessage());
            logEntry.setCreatedAt(now);
            logEntry.setUpdatedAt(now);
            actionLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to record {} connection test", config.getConnectorKey(), ex);
        }
    }

    private CiConnectionTestResultVO result(
            String connectorKey,
            String provider,
            String status,
            String message,
            String requestUrl,
            Long latencyMs) {
        CiConnectionTestResultVO result = new CiConnectionTestResultVO();
        result.setConnectorKey(connectorKey);
        result.setProvider(provider);
        result.setStatus(status);
        result.setMessage(message);
        result.setRequestUrl(requestUrl);
        result.setLatencyMs(latencyMs);
        result.setTestedAt(LocalDateTime.now());
        return result;
    }

    private String baseUrl(URI uri) {
        String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
        return uri.getScheme() + "://" + uri.getHost() + port;
    }

    private String trimGitSuffix(String value) {
        return value.endsWith(".git") ? value.substring(0, value.length() - 4) : value;
    }

    private void require(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalStateException(name + " is missing");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
