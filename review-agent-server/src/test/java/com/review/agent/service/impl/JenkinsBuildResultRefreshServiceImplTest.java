package com.review.agent.service.impl;

import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.ci.JenkinsBuildResultClient;
import com.review.agent.infrastructure.ci.JenkinsBuildSnapshot;
import com.review.agent.infrastructure.ci.JenkinsQueueSnapshot;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.CiStatusWritebackLogMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JenkinsBuildResultRefreshServiceImplTest {

    private final CiStatusConfigMapper configMapper = mock(CiStatusConfigMapper.class);
    private final CiStatusWritebackLogMapper logMapper = mock(CiStatusWritebackLogMapper.class);
    private final FakeJenkinsBuildResultClient client = new FakeJenkinsBuildResultClient();
    private final JenkinsBuildResultRefreshServiceImpl service = new JenkinsBuildResultRefreshServiceImpl(
            configMapper,
            logMapper,
            client);

    @Test
    void refreshesQueueAndBuildResultIntoWritebackLog() {
        CiStatusConfig config = new CiStatusConfig();
        config.setConnectorKey("jenkins-pipeline");
        config.setRepoUrl("https://jenkins.example.com");
        config.setApiToken("token");
        when(configMapper.selectOne(any())).thenReturn(config);

        CiStatusWritebackLog log = new CiStatusWritebackLog();
        log.setId(7L);
        log.setProvider("JENKINS");
        log.setWritebackStatus("SUCCESS");
        log.setRequestUrl("https://jenkins.example.com/queue/item/99/");
        when(logMapper.selectList(any())).thenReturn(List.of(log));

        int refreshed = service.refreshRecent(10);

        assertEquals(1, refreshed);
        assertEquals("https://jenkins.example.com/queue/item/99/", log.getExternalQueueUrl());
        assertEquals("https://jenkins.example.com/job/review-agent-ci/12/", log.getExternalBuildUrl());
        assertEquals("12", log.getExternalBuildNumber());
        assertEquals("SUCCESS", log.getExternalBuildResult());
        verify(logMapper).updateById(log);
    }

    private static class FakeJenkinsBuildResultClient implements JenkinsBuildResultClient {
        @Override
        public JenkinsQueueSnapshot fetchQueue(CiStatusConfig config, String queueUrl) {
            return new JenkinsQueueSnapshot(
                    queueUrl,
                    "https://jenkins.example.com/job/review-agent-ci/12/",
                    "12",
                    "BUILDING");
        }

        @Override
        public JenkinsBuildSnapshot fetchBuild(CiStatusConfig config, String buildUrl) {
            return new JenkinsBuildSnapshot(buildUrl, "12", "SUCCESS", false);
        }
    }
}
