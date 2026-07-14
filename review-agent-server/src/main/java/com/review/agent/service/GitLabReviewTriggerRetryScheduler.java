package com.review.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitLabReviewTriggerRetryScheduler {

    private final GitLabReviewTriggerRetryService retryService;

    @Scheduled(fixedDelayString = "${review-agent.webhook-review.retry-delay-ms:60000}")
    public void retryDueTriggers() {
        retryService.retryDueTriggers();
    }
}
