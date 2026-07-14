package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisIntegrationWebhookReviewTriggerRepository implements IntegrationWebhookReviewTriggerRepository {

    private static final String PROCESSING = "PROCESSING";
    private static final String FAILED = "FAILED";

    private final IntegrationWebhookReviewTriggerMapper mapper;

    @Override
    public boolean tryReserve(
            String connectorKey,
            String triggerKey,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String mergeRequestIid,
            String mergeRequestUrl) {
        return tryReserveInternal(
                connectorKey,
                triggerKey,
                projectId,
                sourceBranch,
                targetBranch,
                mergeRequestIid,
                mergeRequestUrl,
                mergeRequestIid,
                mergeRequestUrl,
                null);
    }

    @Override
    public boolean tryReserve(
            String connectorKey,
            String triggerKey,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String externalEventId,
            String externalEventUrl,
            String commitSha) {
        return tryReserveInternal(
                connectorKey,
                triggerKey,
                projectId,
                sourceBranch,
                targetBranch,
                null,
                null,
                externalEventId,
                externalEventUrl,
                commitSha);
    }

    private boolean tryReserveInternal(
            String connectorKey,
            String triggerKey,
            Long projectId,
            String sourceBranch,
            String targetBranch,
            String mergeRequestIid,
            String mergeRequestUrl,
            String externalEventId,
            String externalEventUrl,
            String commitSha) {
        LocalDateTime now = LocalDateTime.now();
        IntegrationWebhookReviewTrigger trigger = new IntegrationWebhookReviewTrigger();
        trigger.setConnectorKey(connectorKey);
        trigger.setTriggerKey(triggerKey);
        trigger.setTriggerStatus(PROCESSING);
        trigger.setProjectId(projectId);
        trigger.setSourceBranch(sourceBranch);
        trigger.setTargetBranch(targetBranch);
        trigger.setMergeRequestIid(mergeRequestIid);
        trigger.setMergeRequestUrl(mergeRequestUrl);
        trigger.setExternalEventId(externalEventId);
        trigger.setExternalEventUrl(externalEventUrl);
        trigger.setCommitSha(commitSha);
        trigger.setRetryCount(0);
        trigger.setCreatedAt(now);
        trigger.setUpdatedAt(now);
        try {
            mapper.insert(trigger);
            return true;
        } catch (DuplicateKeyException ex) {
            return claimFailed(connectorKey, triggerKey);
        }
    }

    @Override
    public Optional<IntegrationWebhookReviewTrigger> find(String connectorKey, String triggerKey) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerKey, triggerKey)));
    }

    @Override
    public Optional<IntegrationWebhookReviewTrigger> findByReviewId(Long reviewId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getReviewId, reviewId)
                .orderByDesc(IntegrationWebhookReviewTrigger::getUpdatedAt)
                .last("LIMIT 1")));
    }

    @Override
    public Optional<IntegrationWebhookReviewTrigger> findByReviewId(String connectorKey, Long reviewId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getReviewId, reviewId)
                .orderByDesc(IntegrationWebhookReviewTrigger::getUpdatedAt)
                .last("LIMIT 1")));
    }

    @Override
    public List<IntegrationWebhookReviewTrigger> findDueFailed(String connectorKey, LocalDateTime now, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerStatus, FAILED)
                .isNotNull(IntegrationWebhookReviewTrigger::getNextRetryAt)
                .le(IntegrationWebhookReviewTrigger::getNextRetryAt, now)
                .orderByAsc(IntegrationWebhookReviewTrigger::getNextRetryAt)
                .last("LIMIT " + Math.max(1, limit)));
    }

    @Override
    public boolean claimFailed(String connectorKey, String triggerKey) {
        return claim(connectorKey, triggerKey, List.of(FAILED));
    }

    @Override
    public boolean claimRetryable(String connectorKey, String triggerKey) {
        return claim(connectorKey, triggerKey, List.of(FAILED, "EXHAUSTED"));
    }

    private boolean claim(String connectorKey, String triggerKey, List<String> statuses) {
        return mapper.update(null, new LambdaUpdateWrapper<IntegrationWebhookReviewTrigger>()
                .set(IntegrationWebhookReviewTrigger::getTriggerStatus, PROCESSING)
                .set(IntegrationWebhookReviewTrigger::getMessage, null)
                .set(IntegrationWebhookReviewTrigger::getNextRetryAt, null)
                .set(IntegrationWebhookReviewTrigger::getUpdatedAt, LocalDateTime.now())
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerKey, triggerKey)
                .in(IntegrationWebhookReviewTrigger::getTriggerStatus, statuses)) > 0;
    }

    @Override
    public void markProcessed(String connectorKey, String triggerKey, Long reviewId) {
        mapper.update(null, new LambdaUpdateWrapper<IntegrationWebhookReviewTrigger>()
                .set(IntegrationWebhookReviewTrigger::getTriggerStatus, "PROCESSED")
                .set(IntegrationWebhookReviewTrigger::getReviewId, reviewId)
                .set(IntegrationWebhookReviewTrigger::getMessage, null)
                .set(IntegrationWebhookReviewTrigger::getNextRetryAt, null)
                .set(IntegrationWebhookReviewTrigger::getUpdatedAt, LocalDateTime.now())
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerKey, triggerKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerStatus, PROCESSING));
    }

    @Override
    public void markFailed(String connectorKey, String triggerKey, String message, LocalDateTime nextRetryAt) {
        mapper.update(null, new LambdaUpdateWrapper<IntegrationWebhookReviewTrigger>()
                .set(IntegrationWebhookReviewTrigger::getTriggerStatus, FAILED)
                .set(IntegrationWebhookReviewTrigger::getMessage, message)
                .set(IntegrationWebhookReviewTrigger::getNextRetryAt, nextRetryAt)
                .setSql("retry_count = retry_count + 1")
                .set(IntegrationWebhookReviewTrigger::getUpdatedAt, LocalDateTime.now())
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerKey, triggerKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerStatus, PROCESSING));
    }

    @Override
    public void markExhausted(String connectorKey, String triggerKey, String message) {
        mapper.update(null, new LambdaUpdateWrapper<IntegrationWebhookReviewTrigger>()
                .set(IntegrationWebhookReviewTrigger::getTriggerStatus, "EXHAUSTED")
                .set(IntegrationWebhookReviewTrigger::getMessage, message)
                .set(IntegrationWebhookReviewTrigger::getNextRetryAt, null)
                .setSql("retry_count = retry_count + 1")
                .set(IntegrationWebhookReviewTrigger::getUpdatedAt, LocalDateTime.now())
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerKey, triggerKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerStatus, PROCESSING));
    }

    @Override
    public long countByStatus(String connectorKey, String status) {
        return mapper.selectCount(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .eq(IntegrationWebhookReviewTrigger::getTriggerStatus, status));
    }

    @Override
    public Optional<IntegrationWebhookReviewTrigger> findOldestPending(String connectorKey) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<IntegrationWebhookReviewTrigger>()
                .eq(IntegrationWebhookReviewTrigger::getConnectorKey, connectorKey)
                .in(IntegrationWebhookReviewTrigger::getTriggerStatus, PROCESSING, FAILED, "EXHAUSTED")
                .orderByAsc(IntegrationWebhookReviewTrigger::getUpdatedAt)
                .last("LIMIT 1")));
    }
}
