package com.review.agent.service.impl;

import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.ci.PrePrGateCiStatusPublisher;
import com.review.agent.infrastructure.persistence.CiStatusWritebackRetryRepository;
import com.review.agent.service.CiStatusWritebackRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CiStatusWritebackRetryServiceImpl implements CiStatusWritebackRetryService {

    private final CiStatusWritebackRetryRepository retryRepository;
    private final PrePrGateCiStatusPublisher prePrGateCiStatusPublisher;
    private static final int DUE_RETRY_BATCH_SIZE = 20;

    @Override
    public void retry(Long writebackLogId) {
        CiStatusWritebackLog log = retryRepository.findById(writebackLogId)
                .orElseThrow(() -> new IllegalArgumentException("CI writeback log not found: " + writebackLogId));
        retryFailedLog(log);
    }

    @Override
    public int retryDueWritebacks() {
        List<CiStatusWritebackLog> dueLogs = retryRepository.findDueFailedWritebacks(
                LocalDateTime.now(),
                DUE_RETRY_BATCH_SIZE);
        int retried = 0;
        for (CiStatusWritebackLog dueLog : dueLogs) {
            try {
                retryFailedLog(dueLog);
                retried++;
            } catch (RuntimeException ex) {
                log.warn("Skip due CI writeback retry for log {}: {}", dueLog.getId(), ex.getMessage());
            }
        }
        return retried;
    }

    private void retryFailedLog(CiStatusWritebackLog log) {
        if (!"FAILED".equals(log.getWritebackStatus())) {
            throw new IllegalStateException("Only failed CI writebacks can be retried");
        }
        if (log.getReviewId() == null) {
            throw new IllegalStateException("CI writeback log has no review id");
        }

        log.setRetryCount(log.getRetryCount() == null ? 1 : log.getRetryCount() + 1);
        log.setNextRetryAt(null);
        log.setUpdatedAt(LocalDateTime.now());
        retryRepository.markRetrying(log);

        prePrGateCiStatusPublisher.publishGateStatus(log.getReviewId());
    }
}
