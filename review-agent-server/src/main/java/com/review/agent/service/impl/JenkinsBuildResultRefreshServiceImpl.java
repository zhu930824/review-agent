package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.ci.JenkinsBuildResultClient;
import com.review.agent.infrastructure.ci.JenkinsBuildSnapshot;
import com.review.agent.infrastructure.ci.JenkinsQueueSnapshot;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.CiStatusWritebackLogMapper;
import com.review.agent.service.JenkinsBuildResultRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsBuildResultRefreshServiceImpl implements JenkinsBuildResultRefreshService {

    private static final String PROVIDER = "JENKINS";

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final CiStatusWritebackLogMapper writebackLogMapper;
    private final JenkinsBuildResultClient buildResultClient;

    @Override
    public int refreshRecent(int limit) {
        int refreshed = 0;
        for (CiStatusWritebackLog writebackLog : listRefreshableLogs(limit)) {
            try {
                CiStatusConfig config = loadConfig(writebackLog.getConnectorKey());
                if (config != null && refreshLog(config, writebackLog)) {
                    refreshed++;
                }
            } catch (RuntimeException ex) {
                log.warn("Skip Jenkins build result refresh for writeback {}: {}", writebackLog.getId(), ex.getMessage());
            }
        }
        return refreshed;
    }

    private boolean refreshLog(CiStatusConfig config, CiStatusWritebackLog log) {
        String queueUrl = firstText(log.getExternalQueueUrl(), log.getRequestUrl());
        if (!hasText(queueUrl)) {
            return false;
        }

        JenkinsQueueSnapshot queue = buildResultClient.fetchQueue(config, queueUrl);
        log.setExternalQueueUrl(queue.queueUrl());
        if (hasText(queue.buildUrl())) {
            log.setExternalBuildUrl(queue.buildUrl());
        }
        if (hasText(queue.buildNumber())) {
            log.setExternalBuildNumber(queue.buildNumber());
        }
        log.setExternalBuildResult(queue.result());

        if (hasText(log.getExternalBuildUrl())) {
            JenkinsBuildSnapshot build = buildResultClient.fetchBuild(config, log.getExternalBuildUrl());
            log.setExternalBuildUrl(build.buildUrl());
            if (hasText(build.buildNumber())) {
                log.setExternalBuildNumber(build.buildNumber());
            }
            log.setExternalBuildResult(build.result());
        }

        log.setExternalResultUpdatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        writebackLogMapper.updateById(log);
        return true;
    }

    private CiStatusConfig loadConfig(String connectorKey) {
        return ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, connectorKey));
    }

    private List<CiStatusWritebackLog> listRefreshableLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return writebackLogMapper.selectList(new LambdaQueryWrapper<CiStatusWritebackLog>()
                .eq(CiStatusWritebackLog::getProvider, PROVIDER)
                .eq(CiStatusWritebackLog::getWritebackStatus, "SUCCESS")
                .and(wrapper -> wrapper
                        .isNull(CiStatusWritebackLog::getExternalBuildResult)
                        .or()
                        .in(CiStatusWritebackLog::getExternalBuildResult, "QUEUED", "BUILDING"))
                .orderByDesc(CiStatusWritebackLog::getCreatedAt)
                .last("LIMIT " + safeLimit));
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
