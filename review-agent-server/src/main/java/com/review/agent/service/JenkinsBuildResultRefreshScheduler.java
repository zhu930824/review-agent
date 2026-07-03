package com.review.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JenkinsBuildResultRefreshScheduler {

    private final JenkinsBuildResultRefreshService refreshService;

    @Value("${review-agent.ci-writeback.jenkins-refresh-limit:20}")
    private int refreshLimit;

    @Scheduled(fixedDelayString = "${review-agent.ci-writeback.jenkins-refresh-delay-ms:60000}")
    public void refreshRecentJenkinsResults() {
        refreshService.refreshRecent(refreshLimit);
    }
}
