package com.review.agent.service;

import com.review.agent.infrastructure.webhook.GitLabMergeRequestReviewTriggerResult;

public interface GitLabReviewTriggerRetryService {

    GitLabMergeRequestReviewTriggerResult retry(String triggerKey);

    int retryDueTriggers();
}
