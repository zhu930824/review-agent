package com.review.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CiStatusWritebackRetryScheduler {

    private final CiStatusWritebackRetryService retryService;

    @Scheduled(fixedDelayString = "${review-agent.ci-writeback.retry-delay-ms:60000}")
    public void retryDueWritebacks() {
        retryService.retryDueWritebacks();
    }
}
