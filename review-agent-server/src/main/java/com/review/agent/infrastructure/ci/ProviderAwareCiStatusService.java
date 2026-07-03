package com.review.agent.infrastructure.ci;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class ProviderAwareCiStatusService implements CiStatusService {

    private static final List<String> CONNECTOR_PRIORITY = List.of(
            "github-checks",
            "gitlab-merge-request",
            "jenkins-pipeline");

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final List<ProviderCiStatusReporter> reporters;

    @Override
    public void reportPass(Long reviewId, String description) {
        report(reviewId, description, ProviderCiStatusReporter::reportPass);
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        report(reviewId, description, ProviderCiStatusReporter::reportBlock);
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        report(reviewId, description, ProviderCiStatusReporter::reportRunning);
    }

    private void report(Long reviewId, String description, CiReporterCall call) {
        Map<String, ProviderCiStatusReporter> reporterByConnector = reporters.stream()
                .collect(Collectors.toMap(ProviderCiStatusReporter::connectorKey, reporter -> reporter));
        boolean reported = false;
        for (String connectorKey : CONNECTOR_PRIORITY) {
            CiStatusConfig config = loadConfig(connectorKey);
            if (!isEnabled(config)) {
                continue;
            }
            ProviderCiStatusReporter reporter = reporterByConnector.get(connectorKey);
            if (reporter == null) {
                log.info("[CI-Status] no reporter registered for enabled connector={}", connectorKey);
                continue;
            }
            call.accept(reporter, reviewId, description);
            reported = true;
        }

        ProviderCiStatusReporter fallback = reporterByConnector.get("github-checks");
        if (!reported && fallback != null) {
            call.accept(fallback, reviewId, description);
        }
    }

    private CiStatusConfig loadConfig(String connectorKey) {
        return ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, connectorKey));
    }

    private boolean isEnabled(CiStatusConfig config) {
        return config != null && Boolean.TRUE.equals(config.getChecksEnabled());
    }

    @FunctionalInterface
    private interface CiReporterCall {
        void accept(ProviderCiStatusReporter reporter, Long reviewId, String description);
    }
}
