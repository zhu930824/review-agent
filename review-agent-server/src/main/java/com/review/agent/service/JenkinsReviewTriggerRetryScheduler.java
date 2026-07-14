package com.review.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JenkinsReviewTriggerRetryScheduler {

    private final JenkinsReviewTriggerRetryService retryService;

    @Scheduled(fixedDelayString = "${review-agent.jenkins-review.retry-delay-ms:60000}")
    public void retryDueTriggers() {
        retryService.retryDueTriggers();
    }
}
