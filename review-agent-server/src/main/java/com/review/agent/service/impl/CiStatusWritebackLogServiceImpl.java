package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.CiStatusWritebackLogVO;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.persistence.CiStatusWritebackLogMapper;
import com.review.agent.service.CiStatusWritebackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CiStatusWritebackLogServiceImpl implements CiStatusWritebackLogService {

    private static final String CONNECTOR_KEY = "github-checks";
    private static final String PROVIDER = "GITHUB";
    private static final Map<String, String> KNOWN_PROVIDERS = knownProviders();

    private final CiStatusWritebackLogMapper ciStatusWritebackLogMapper;

    @Override
    public List<CiStatusWritebackLogVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return ciStatusWritebackLogMapper.selectList(
                new LambdaQueryWrapper<CiStatusWritebackLog>()
                        .orderByDesc(CiStatusWritebackLog::getCreatedAt)
                        .last("LIMIT " + safeLimit))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<CiIntegrationHealthVO> listHealth(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<CiStatusWritebackLog> logs = ciStatusWritebackLogMapper.selectList(
                new LambdaQueryWrapper<CiStatusWritebackLog>()
                        .orderByDesc(CiStatusWritebackLog::getCreatedAt)
                        .last("LIMIT " + safeLimit));
        Map<String, List<CiStatusWritebackLog>> logsByConnector = logs.stream()
                .collect(LinkedHashMap::new,
                        (map, log) -> map.computeIfAbsent(log.getConnectorKey(), ignored -> new java.util.ArrayList<>()).add(log),
                        Map::putAll);

        Map<String, String> providers = new LinkedHashMap<>(KNOWN_PROVIDERS);
        logs.forEach(log -> providers.putIfAbsent(log.getConnectorKey(), log.getProvider()));

        return providers.entrySet().stream()
                .map(entry -> toHealth(entry.getKey(), entry.getValue(), logsByConnector.getOrDefault(entry.getKey(), List.of())))
                .toList();
    }

    @Override
    public void recordSuccess(Long reviewId, String commitSha, String state, String requestUrl) {
        recordSuccess(CONNECTOR_KEY, PROVIDER, reviewId, commitSha, state, requestUrl);
    }

    @Override
    public void recordSuccess(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl) {
        insert(connectorKey, provider, reviewId, commitSha, state, "SUCCESS", requestUrl, null, null);
    }

    @Override
    public void recordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        recordFailure(CONNECTOR_KEY, PROVIDER, reviewId, commitSha, state, requestUrl, errorMessage);
    }

    @Override
    public void recordFailure(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        insert(connectorKey, provider, reviewId, commitSha, state, "FAILED", requestUrl, truncate(errorMessage), LocalDateTime.now().plusMinutes(5));
    }

    @Override
    public void recordSkipped(Long reviewId, String state, String reason) {
        recordSkipped(CONNECTOR_KEY, PROVIDER, reviewId, state, reason);
    }

    @Override
    public void recordSkipped(String connectorKey, String provider, Long reviewId, String state, String reason) {
        insert(connectorKey, provider, reviewId, null, state, "SKIPPED", null, truncate(reason), null);
    }

    private void insert(
            String connectorKey,
            String provider,
            Long reviewId,
            String commitSha,
            String state,
            String writebackStatus,
            String requestUrl,
            String errorMessage,
            LocalDateTime nextRetryAt) {
        LocalDateTime now = LocalDateTime.now();
        CiStatusWritebackLog log = new CiStatusWritebackLog();
        log.setConnectorKey(hasText(connectorKey) ? connectorKey : CONNECTOR_KEY);
        log.setProvider(hasText(provider) ? provider : PROVIDER);
        log.setReviewId(reviewId);
        log.setCommitSha(commitSha);
        log.setState(state);
        log.setWritebackStatus(writebackStatus);
        log.setRequestUrl(requestUrl);
        if ("JENKINS".equalsIgnoreCase(provider) && hasText(requestUrl)) {
            log.setExternalQueueUrl(requestUrl);
        }
        log.setErrorMessage(errorMessage);
        log.setRetryCount(0);
        log.setNextRetryAt(nextRetryAt);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        ciStatusWritebackLogMapper.insert(log);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 2000) {
            return message;
        }
        return message.substring(0, 2000);
    }

    private static Map<String, String> knownProviders() {
        Map<String, String> providers = new LinkedHashMap<>();
        providers.put("github-checks", "GITHUB");
        providers.put("gitlab-merge-request", "GITLAB");
        providers.put("jenkins-pipeline", "JENKINS");
        return providers;
    }

    private CiIntegrationHealthVO toHealth(String connectorKey, String provider, List<CiStatusWritebackLog> logs) {
        CiStatusWritebackLog latest = logs.stream()
                .max(Comparator.comparing(
                        CiStatusWritebackLog::getCreatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        long total = logs.size();
        long success = logs.stream().filter(log -> "SUCCESS".equals(log.getWritebackStatus())).count();
        long failed = logs.stream().filter(log -> "FAILED".equals(log.getWritebackStatus())).count();
        long skipped = logs.stream().filter(log -> "SKIPPED".equals(log.getWritebackStatus())).count();

        CiIntegrationHealthVO vo = new CiIntegrationHealthVO();
        vo.setConnectorKey(connectorKey);
        vo.setProvider(provider);
        vo.setTotalCount(total);
        vo.setSuccessCount(success);
        vo.setFailedCount(failed);
        vo.setSkippedCount(skipped);
        if (latest != null) {
            vo.setLatestWritebackId(latest.getId());
            vo.setLatestWritebackStatus(latest.getWritebackStatus());
            vo.setLatestExternalResult(latest.getExternalBuildResult());
            vo.setLatestRequestUrl(latest.getRequestUrl());
            vo.setLatestExternalQueueUrl(latest.getExternalQueueUrl());
            vo.setLatestExternalBuildUrl(latest.getExternalBuildUrl());
            vo.setLatestAt(latest.getCreatedAt());
        }
        vo.setHealthStatus(resolveHealthStatus(latest, failed, total));
        vo.setSummary(healthSummary(vo));
        return vo;
    }

    private String resolveHealthStatus(CiStatusWritebackLog latest, long failedCount, long totalCount) {
        if (totalCount == 0 || latest == null) {
            return "NO_DATA";
        }
        if ("FAILED".equals(latest.getWritebackStatus()) || "FAILURE".equalsIgnoreCase(latest.getExternalBuildResult())) {
            return "UNHEALTHY";
        }
        if (failedCount > 0
                || "SKIPPED".equals(latest.getWritebackStatus())
                || "UNSTABLE".equalsIgnoreCase(latest.getExternalBuildResult())
                || "ABORTED".equalsIgnoreCase(latest.getExternalBuildResult())
                || "CANCELLED".equalsIgnoreCase(latest.getExternalBuildResult())
                || "BUILDING".equalsIgnoreCase(latest.getExternalBuildResult())
                || "QUEUED".equalsIgnoreCase(latest.getExternalBuildResult())) {
            return "DEGRADED";
        }
        return "HEALTHY";
    }

    private String healthSummary(CiIntegrationHealthVO health) {
        if ("NO_DATA".equals(health.getHealthStatus())) {
            return "No recent writeback data.";
        }
        String latest = health.getLatestExternalResult() == null
                ? health.getLatestWritebackStatus()
                : health.getLatestWritebackStatus() + " / " + health.getLatestExternalResult();
        return "Recent " + health.getTotalCount()
                + " writebacks: " + health.getSuccessCount() + " success, "
                + health.getFailedCount() + " failed, "
                + health.getSkippedCount() + " skipped. Latest: " + latest + ".";
    }

    private CiStatusWritebackLogVO toVO(CiStatusWritebackLog log) {
        CiStatusWritebackLogVO vo = new CiStatusWritebackLogVO();
        vo.setId(log.getId());
        vo.setConnectorKey(log.getConnectorKey());
        vo.setProvider(log.getProvider());
        vo.setReviewId(log.getReviewId());
        vo.setCommitSha(log.getCommitSha());
        vo.setState(log.getState());
        vo.setWritebackStatus(log.getWritebackStatus());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setExternalQueueUrl(log.getExternalQueueUrl());
        vo.setExternalBuildUrl(log.getExternalBuildUrl());
        vo.setExternalBuildNumber(log.getExternalBuildNumber());
        vo.setExternalBuildResult(log.getExternalBuildResult());
        vo.setExternalResultUpdatedAt(log.getExternalResultUpdatedAt());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setRetryCount(log.getRetryCount());
        vo.setNextRetryAt(log.getNextRetryAt());
        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());
        return vo;
    }
}
