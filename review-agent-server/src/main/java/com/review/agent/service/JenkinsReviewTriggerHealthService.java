package com.review.agent.service;

import com.review.agent.domain.dto.JenkinsReviewTriggerHealthVO;

public interface JenkinsReviewTriggerHealthService {

    JenkinsReviewTriggerHealthVO getHealth(String connectorKey);
}
