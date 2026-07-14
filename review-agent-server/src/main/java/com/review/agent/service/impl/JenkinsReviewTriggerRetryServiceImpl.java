package com.review.agent.service.impl;

import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.JenkinsReviewTriggerResultVO;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.service.JenkinsReviewTriggerRetryService;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsReviewTriggerRetryServiceImpl implements JenkinsReviewTriggerRetryService {

    private static final String DEFAULT_CONNECTOR_KEY = "jenkins-pipeline";
    private static final int DUE_RETRY_BATCH_SIZE = 20;
    private static final int MAX_RETRY_COUNT = 5;

    private final IntegrationWebhookReviewTriggerRepository triggerRepository;
    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final ReviewService reviewService;
    private final CiStatusConfigMapper configMapper;

    @Override
    public JenkinsReviewTriggerResultVO retry(String connectorKey, String triggerKey) {
        String key = normalizeConnectorKey(connectorKey);
        IntegrationWebhookReviewTrigger trigger = triggerRepository.find(key, triggerKey)
                .orElseThrow(() -> new IllegalArgumentException("Jenkins review trigger not found: " + triggerKey));
        if (!triggerRepository.claimRetryable(key, triggerKey)) {
            throw new IllegalStateException("Jenkins review trigger is not retryable: " + triggerKey);
        }
        return process(key, trigger);
    }

    @Override
    public int retryDueTriggers() {
        int attempted = 0;
        for (CiStatusConfig config : listEnabledJenkinsConfigs()) {
            String connectorKey = config.getConnectorKey();
            List<IntegrationWebhookReviewTrigger> due = triggerRepository.findDueFailed(
                    connectorKey, LocalDateTime.now(), DUE_RETRY_BATCH_SIZE);
            for (IntegrationWebhookReviewTrigger trigger : due) {
                if (!triggerRepository.claimFailed(connectorKey, trigger.getTriggerKey())) {
                    continue;
                }
                process(connectorKey, trigger);
                attempted++;
            }
        }
        return attempted;
    }

    private JenkinsReviewTriggerResultVO process(String connectorKey, IntegrationWebhookReviewTrigger trigger) {
        try {
            Long reviewId = createReview(trigger);
            triggerRepository.markProcessed(connectorKey, trigger.getTriggerKey(), reviewId);
            deliveryRepository.updateTriggerResult(
                    connectorKey, trigger.getTriggerKey(), "PROCESSED", reviewId, null, trigger.getRetryCount(), null);
            return result("PROCESSED", trigger.getTriggerKey(), reviewId, null, trigger.getRetryCount(), null);
        } catch (Exception ex) {
            int retryCount = trigger.getRetryCount() == null ? 1 : trigger.getRetryCount() + 1;
            String message = safeMessage(ex);
            LocalDateTime nextRetryAt = null;
            String status;
            if (retryCount >= MAX_RETRY_COUNT) {
                status = "EXHAUSTED";
                triggerRepository.markExhausted(connectorKey, trigger.getTriggerKey(), message);
            } else {
                status = "FAILED";
                nextRetryAt = LocalDateTime.now().plusMinutes(nextRetryDelayMinutes(retryCount));
                triggerRepository.markFailed(connectorKey, trigger.getTriggerKey(), message, nextRetryAt);
            }
            deliveryRepository.updateTriggerResult(
                    connectorKey, trigger.getTriggerKey(), status, null, message, retryCount, nextRetryAt);
            log.warn("Jenkins review trigger {} failed", trigger.getTriggerKey(), ex);
            return result(status, trigger.getTriggerKey(), null, message, retryCount, nextRetryAt);
        }
    }

    private Long createReview(IntegrationWebhookReviewTrigger trigger) {
        CreatePrePrRequest request = new CreatePrePrRequest();
        request.setProjectId(trigger.getProjectId());
        request.setSourceBranch(trigger.getSourceBranch());
        request.setTargetBranch(trigger.getTargetBranch());
        ReviewDetailVO detail = reviewService.createPrePrReview(request);
        Long reviewId = detail == null || detail.getReview() == null ? null : detail.getReview().getId();
        if (reviewId == null) {
            throw new IllegalStateException("Pre-PR review was created without a review id");
        }
        return reviewId;
    }

    private long nextRetryDelayMinutes(int retryCount) {
        return Math.min(30L, 1L << Math.min(Math.max(1, retryCount), 5));
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private JenkinsReviewTriggerResultVO result(
            String status,
            String triggerKey,
            Long reviewId,
            String message,
            Integer retryCount,
            LocalDateTime nextRetryAt) {
        JenkinsReviewTriggerResultVO result = new JenkinsReviewTriggerResultVO();
        result.setStatus(status);
        result.setTriggerKey(triggerKey);
        result.setReviewId(reviewId);
        result.setMessage(message);
        result.setRetryCount(retryCount);
        result.setNextRetryAt(nextRetryAt);
        return result;
    }

    private List<CiStatusConfig> listEnabledJenkinsConfigs() {
        return configMapper.selectList(new LambdaQueryWrapper<CiStatusConfig>()
                .eq(CiStatusConfig::getProvider, "JENKINS")
                .eq(CiStatusConfig::getChecksEnabled, true));
    }

    private String normalizeConnectorKey(String connectorKey) {
        String key = connectorKey == null || connectorKey.isBlank()
                ? DEFAULT_CONNECTOR_KEY
                : connectorKey.trim().toLowerCase();
        if (!key.equals(DEFAULT_CONNECTOR_KEY) && !key.startsWith(DEFAULT_CONNECTOR_KEY + ":")) {
            throw new IllegalArgumentException("Invalid Jenkins connector key: " + key);
        }
        return key;
    }
}
