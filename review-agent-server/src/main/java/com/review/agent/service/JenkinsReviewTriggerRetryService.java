package com.review.agent.service;

import com.review.agent.domain.dto.JenkinsReviewTriggerResultVO;

public interface JenkinsReviewTriggerRetryService {

    JenkinsReviewTriggerResultVO retry(String connectorKey, String triggerKey);

    int retryDueTriggers();
}
