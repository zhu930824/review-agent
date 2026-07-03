package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;

public interface JenkinsBuildResultClient {

    JenkinsQueueSnapshot fetchQueue(CiStatusConfig config, String queueUrl);

    JenkinsBuildSnapshot fetchBuild(CiStatusConfig config, String buildUrl);
}
