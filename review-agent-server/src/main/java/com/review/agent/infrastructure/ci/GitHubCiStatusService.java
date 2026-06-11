package com.review.agent.infrastructure.ci;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.service.CiStatusWritebackLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class GitHubCiStatusService implements CiStatusService {

    private static final String CONNECTOR_KEY = "github-checks";

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final ReviewMapper reviewMapper;
    private final GitHubStatusRequestFactory requestFactory;
    private final CiStatusWritebackLogService writebackLogService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void reportPass(Long reviewId, String description) {
        report(reviewId, "success", description);
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        report(reviewId, "failure", description);
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        report(reviewId, "pending", description);
    }

    private void report(Long reviewId, String state, String description) {
        CiStatusConfig config = loadConfig();
        if (!isReady(config)) {
            log.info("[CI-Status] github checks config is not ready, skip review={} state={}", reviewId, state);
            safeRecordSkipped(reviewId, state, "github checks config is not ready");
            return;
        }

        Review review = reviewMapper.selectById(reviewId);
        String commitSha = resolveCommitSha(review);
        if (!hasText(commitSha)) {
            log.info("[CI-Status] review={} has no commit sha, skip github status writeback", reviewId);
            safeRecordSkipped(reviewId, state, "review has no commit sha");
            return;
        }

        GitHubStatusRequest request = null;
        try {
            request = requestFactory.build(config, commitSha, state, description, reviewId);
            HttpHeaders headers = new HttpHeaders();
            request.headers().forEach(headers::set);
            restTemplate.postForEntity(request.url(), new HttpEntity<>(request.body(), headers), String.class);
            safeRecordSuccess(reviewId, commitSha, state, request.url());
            log.info("[CI-Status] github status posted review={} state={}", reviewId, state);
        } catch (Exception e) {
            String requestUrl = request == null ? null : request.url();
            safeRecordFailure(reviewId, commitSha, state, requestUrl, e.getMessage());
            log.warn("[CI-Status] github status writeback failed review={} state={}", reviewId, state, e);
        }
    }

    private void safeRecordSuccess(Long reviewId, String commitSha, String state, String requestUrl) {
        try {
            writebackLogService.recordSuccess(reviewId, commitSha, state, requestUrl);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record github status success review={} state={}", reviewId, state, e);
        }
    }

    private void safeRecordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        try {
            writebackLogService.recordFailure(reviewId, commitSha, state, requestUrl, errorMessage);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record github status failure review={} state={}", reviewId, state, e);
        }
    }

    private void safeRecordSkipped(Long reviewId, String state, String reason) {
        try {
            writebackLogService.recordSkipped(reviewId, state, reason);
        } catch (Exception e) {
            log.warn("[CI-Status] failed to record github status skip review={} state={}", reviewId, state, e);
        }
    }

    private CiStatusConfig loadConfig() {
        return ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, CONNECTOR_KEY));
    }

    private boolean isReady(CiStatusConfig config) {
        return config != null
                && Boolean.TRUE.equals(config.getChecksEnabled())
                && "GITHUB".equalsIgnoreCase(config.getProvider())
                && hasText(config.getRepoOwner())
                && hasText(config.getRepoName())
                && hasText(config.getStatusContext())
                && hasText(config.getApiToken());
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
