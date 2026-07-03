package com.review.agent.service.impl;

import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.service.CiStatusWritebackLogService;
import com.review.agent.service.OperationsCiHealthActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsCiHealthActionServiceImpl implements OperationsCiHealthActionService {

    private static final int HEALTH_SAMPLE_LIMIT = 50;

    private final CiStatusWritebackLogService ciStatusWritebackLogService;

    @Override
    public List<OperationsCiHealthActionVO> listActions() {
        return ciStatusWritebackLogService.listHealth(HEALTH_SAMPLE_LIMIT).stream()
                .filter(health -> !"HEALTHY".equals(health.getHealthStatus()))
                .map(this::toAction)
                .toList();
    }

    private OperationsCiHealthActionVO toAction(CiIntegrationHealthVO health) {
        OperationsCiHealthActionVO vo = new OperationsCiHealthActionVO();
        vo.setKey(health.getConnectorKey() + "-" + health.getHealthStatus());
        vo.setConnectorKey(health.getConnectorKey());
        vo.setProvider(health.getProvider());
        vo.setHealthStatus(health.getHealthStatus());
        vo.setSeverity(severity(health.getHealthStatus()));
        vo.setOwnerRole(ownerRole(health.getProvider()));
        vo.setSlaHours(slaHours(health.getHealthStatus()));
        vo.setLatestSignal(latestSignal(health));
        vo.setRecommendation(recommendation(health));
        return vo;
    }

    private String severity(String healthStatus) {
        if ("UNHEALTHY".equals(healthStatus)) {
            return "CRITICAL";
        }
        if ("DEGRADED".equals(healthStatus)) {
            return "WARNING";
        }
        return "INFO";
    }

    private Long slaHours(String healthStatus) {
        if ("UNHEALTHY".equals(healthStatus)) {
            return 4L;
        }
        if ("DEGRADED".equals(healthStatus)) {
            return 24L;
        }
        return 72L;
    }

    private String ownerRole(String provider) {
        if ("JENKINS".equalsIgnoreCase(provider)) {
            return "CI Owner";
        }
        if ("GITLAB".equalsIgnoreCase(provider)) {
            return "GitLab Owner";
        }
        if ("GITHUB".equalsIgnoreCase(provider)) {
            return "GitHub Owner";
        }
        return "Integration Owner";
    }

    private String latestSignal(CiIntegrationHealthVO health) {
        String writeback = health.getLatestWritebackStatus() == null ? "no writeback" : health.getLatestWritebackStatus();
        if (health.getLatestExternalResult() == null || health.getLatestExternalResult().isBlank()) {
            return writeback;
        }
        return writeback + " / " + health.getLatestExternalResult();
    }

    private String recommendation(CiIntegrationHealthVO health) {
        if ("UNHEALTHY".equals(health.getHealthStatus())) {
            return "Check credentials, repository binding, permissions, and the latest failed writeback before the next release gate.";
        }
        if ("DEGRADED".equals(health.getHealthStatus())) {
            return "Review retry history and external build state; refresh Jenkins results when this action belongs to a pipeline connector.";
        }
        return "Run a gate publish smoke test so the workbench has fresh CI evidence for this connector.";
    }
}
