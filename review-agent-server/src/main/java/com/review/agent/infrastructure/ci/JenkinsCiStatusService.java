package com.review.agent.infrastructure.ci;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.service.CiStatusWritebackLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsCiStatusService implements ProviderCiStatusReporter {

    private static final String CONNECTOR_KEY = "jenkins-pipeline";
    private static final String PROVIDER = "JENKINS";

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final ReviewMapper reviewMapper;
    private final JenkinsGateRequestFactory requestFactory;
    private final CiStatusWritebackLogService writebackLogService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String connectorKey() {
        return CONNECTOR_KEY;
    }

    @Override
    public void reportPass(Long reviewId, String description) {
        report(loadConfig(), reviewId, "success", description);
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        report(loadConfig(), reviewId, "failure", description);
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        report(loadConfig(), reviewId, "pending", description);
    }

    @Override
    public void reportPass(CiStatusConfig config, Long reviewId, String description) {
        report(config, reviewId, "success", description);
    }

    @Override
    public void reportBlock(CiStatusConfig config, Long reviewId, String description) {
        report(config, reviewId, "failure", description);
    }

    @Override
    public void reportRunning(CiStatusConfig config, Long reviewId, String description) {
        report(config, reviewId, "pending", description);
    }

    private void report(CiStatusConfig config, Long reviewId, String state, String description) {
        if (!isReady(config)) {
            log.info("[CI-Status] jenkins config is not ready, skip review={} state={}", reviewId, state);
            safeRecordSkipped(config == null ? CONNECTOR_KEY : config.getConnectorKey(), reviewId, state,
                    "jenkins config is not ready");
            return;
        }

        Review review = reviewMapper.selectById(reviewId);
        if (!matchesProject(config, review)) {
            return;
        }
        String commitSha = resolveCommitSha(review);

        CiProviderStatusRequest request = null;
        try {
            request = requestFactory.build(config, commitSha, state, description, reviewId);
            request = requestFactory.withCrumb(request, fetchCrumb(config));
            HttpHeaders headers = new HttpHeaders();
            request.headers().forEach(headers::set);
            ResponseEntity<String> response = restTemplate.postForEntity(request.url(), new HttpEntity<>(request.body(), headers), String.class);
            safeRecordSuccess(config.getConnectorKey(), reviewId, commitSha, state, response.getHeaders().getFirst("Location"), request.url());
            log.info("[CI-Status] jenkins gate job triggered review={} state={}", reviewId, state);
        } catch (Exception e) {
            String requestUrl = request == null ? null : request.url();
            safeRecordFailure(config.getConnectorKey(), reviewId, commitSha, state, requestUrl, e.getMessage());
            log.warn("[CI-Status] jenkins gate writeback failed review={} state={}", reviewId, state, e);
        }
    }

    private JenkinsCrumb fetchCrumb(CiStatusConfig config) {
        try {
            HttpHeaders headers = new HttpHeaders();
            requestFactory.buildCrumbHeaders(config).forEach(headers::set);
            ResponseEntity<Map> response = restTemplate.exchange(
                    requestFactory.buildCrumbUrl(config),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);
            Map<?, ?> body = response.getBody();
            if (body == null) {
                return null;
            }
            Object field = body.get("crumbRequestField");
            Object value = body.get("crumb");
            if (field == null || value == null) {
                return null;
            }
            return new JenkinsCrumb(String.valueOf(field), String.valueOf(value));
        } catch (Exception e) {
            log.info("[CI-Status] jenkins crumb fetch skipped: {}", e.getMessage());
            return null;
        }
    }

    private CiStatusConfig loadConfig() {
        return ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, CONNECTOR_KEY));
    }

    private boolean isReady(CiStatusConfig config) {
        return config != null
                && Boolean.TRUE.equals(config.getChecksEnabled())
                && PROVIDER.equalsIgnoreCase(config.getProvider())
                && hasText(config.getRepoUrl())
                && hasText(config.getRepoName())
                && hasText(config.getStatusContext())
                && hasText(config.getApiToken());
    }

    private boolean matchesProject(CiStatusConfig config, Review review) {
        return config.getProjectId() == null
                || (review != null && config.getProjectId().equals(review.getProjectId()));
    }

    private String resolveCommitSha(Review review) {
        if (review == null) {
            return null;
        }
        if (hasText(review.getSourceCommit())) {
            return review.getSourceCommit();
        }
        return review.getTargetCommit();
    }

    private void safeRecordSuccess(String connectorKey, Long reviewId, String commitSha, String state, String queueUrl, String requestUrl) {
        try {
            writebackLogService.recordSuccess(connectorKey, PROVIDER, reviewId, commitSha, state, hasText(queueUrl) ? queueUrl : requestUrl);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record jenkins gate success review={} state={}", reviewId, state, e);
        }
    }

    private void safeRecordFailure(String connectorKey, Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        try {
            writebackLogService.recordFailure(connectorKey, PROVIDER, reviewId, commitSha, state, requestUrl, errorMessage);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record jenkins gate failure review={} state={}", reviewId, state, e);
        }
    }

    private void safeRecordSkipped(String connectorKey, Long reviewId, String state, String reason) {
        try {
            writebackLogService.recordSkipped(connectorKey, PROVIDER, reviewId, state, reason);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record jenkins gate skip review={} state={}", reviewId, state, e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
