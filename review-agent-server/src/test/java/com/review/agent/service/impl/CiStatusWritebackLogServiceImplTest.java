package com.review.agent.service.impl;

import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.persistence.CiStatusWritebackLogMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CiStatusWritebackLogServiceImplTest {

    private final CiStatusWritebackLogMapper logMapper = mock(CiStatusWritebackLogMapper.class);
    private final CiStatusWritebackLogServiceImpl service = new CiStatusWritebackLogServiceImpl(logMapper);

    @Test
    void listsKnownProviderHealthFromRecentWritebacks() {
        LocalDateTime now = LocalDateTime.now();
        when(logMapper.selectList(any())).thenReturn(List.of(
                log("github-checks", "GITHUB", "SUCCESS", null, now),
                log("gitlab-merge-request", "GITLAB", "FAILED", null, now.minusMinutes(1)),
                log("jenkins-pipeline", "JENKINS", "SUCCESS", "BUILDING", now.minusMinutes(2))));

        List<CiIntegrationHealthVO> health = service.listHealth(50);

        Map<String, CiIntegrationHealthVO> byConnector = health.stream()
                .collect(Collectors.toMap(CiIntegrationHealthVO::getConnectorKey, item -> item));
        assertEquals("HEALTHY", byConnector.get("github-checks").getHealthStatus());
        assertEquals("UNHEALTHY", byConnector.get("gitlab-merge-request").getHealthStatus());
        assertEquals("DEGRADED", byConnector.get("jenkins-pipeline").getHealthStatus());
        assertEquals(1L, byConnector.get("github-checks").getTotalCount());
        assertTrue(byConnector.get("jenkins-pipeline").getSummary().contains("BUILDING"));
    }

    @Test
    void includesKnownProvidersWithoutRecentData() {
        when(logMapper.selectList(any())).thenReturn(List.of());

        List<CiIntegrationHealthVO> health = service.listHealth(50);

        assertEquals(3, health.size());
        assertTrue(health.stream().allMatch(item -> "NO_DATA".equals(item.getHealthStatus())));
    }

    private CiStatusWritebackLog log(
            String connectorKey,
            String provider,
            String writebackStatus,
            String externalBuildResult,
            LocalDateTime createdAt) {
        CiStatusWritebackLog log = new CiStatusWritebackLog();
        log.setConnectorKey(connectorKey);
        log.setProvider(provider);
        log.setWritebackStatus(writebackStatus);
        log.setExternalBuildResult(externalBuildResult);
        log.setCreatedAt(createdAt);
        return log;
    }
}
