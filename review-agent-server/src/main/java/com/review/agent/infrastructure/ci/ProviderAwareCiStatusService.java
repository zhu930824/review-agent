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

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final List<ProviderCiStatusReporter> reporters;

    @Override
    public void reportPass(Long reviewId, String description) {
        report(reviewId, description, (reporter, config, id, text) -> reporter.reportPass(config, id, text));
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        report(reviewId, description, (reporter, config, id, text) -> reporter.reportBlock(config, id, text));
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        report(reviewId, description, (reporter, config, id, text) -> reporter.reportRunning(config, id, text));
    }

    private void report(Long reviewId, String description, CiReporterCall call) {
        Map<String, ProviderCiStatusReporter> reporterByConnector = reporters.stream()
                .collect(Collectors.toMap(ProviderCiStatusReporter::connectorKey, reporter -> reporter));
        boolean reported = false;
        for (CiStatusConfig config : loadConfigs()) {
            if (!isEnabled(config)) {
                continue;
            }
            String connectorKey = config.getConnectorKey();
            ProviderCiStatusReporter reporter = reporterByConnector.get(connectorFamily(connectorKey));
            if (reporter == null) {
                log.info("[CI-Status] no reporter registered for enabled connector={}", connectorKey);
                continue;
            }
            call.accept(reporter, config, reviewId, description);
            reported = true;
        }

        ProviderCiStatusReporter fallback = reporterByConnector.get("github-checks");
        if (!reported && fallback != null) {
            call.accept(fallback, null, reviewId, description);
        }
    }

    private List<CiStatusConfig> loadConfigs() {
        return ciStatusConfigMapper.selectList(new LambdaQueryWrapper<CiStatusConfig>()
                .orderByAsc(CiStatusConfig::getId));
    }

    private String connectorFamily(String connectorKey) {
        if (connectorKey != null && connectorKey.startsWith("jenkins-pipeline:")) {
            return "jenkins-pipeline";
        }
        return connectorKey;
    }

    private boolean isEnabled(CiStatusConfig config) {
        return config != null && Boolean.TRUE.equals(config.getChecksEnabled());
    }

    @FunctionalInterface
    private interface CiReporterCall {
        void accept(
                ProviderCiStatusReporter reporter,
                CiStatusConfig config,
                Long reviewId,
                String description);
    }
}
