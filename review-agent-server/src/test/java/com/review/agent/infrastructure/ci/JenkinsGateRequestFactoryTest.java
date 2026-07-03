package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JenkinsGateRequestFactoryTest {

    private final JenkinsGateRequestFactory factory = new JenkinsGateRequestFactory();

    @Test
    void buildsParameterizedGateJobRequest() {
        CiStatusConfig config = new CiStatusConfig();
        config.setRepoUrl("https://jenkins.example.com/");
        config.setRepoOwner("platform/review");
        config.setRepoName("review-agent-ci");
        config.setStatusContext("Review Agent Gate");
        config.setApiToken("jenkins-token");
        config.setWebhookSecret("codex");

        CiProviderStatusRequest request = factory.build(config, "abc123", "success", "review passed", 42L);

        assertEquals("https://jenkins.example.com/job/platform/job/review/job/review-agent-ci/buildWithParameters", request.url());
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString("codex:jenkins-token".getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedAuth, request.headers().get("Authorization"));
        assertEquals("PASSED", request.body().get("REVIEW_AGENT_STATE"));
        assertEquals("Review Agent Gate", request.body().get("REVIEW_AGENT_CONTEXT"));
        assertEquals("42", request.body().get("REVIEW_AGENT_REVIEW_ID"));
        assertEquals("abc123", request.body().get("REVIEW_AGENT_COMMIT_SHA"));
    }

    @Test
    void buildsCrumbRequestAndMergesCrumbHeader() {
        CiStatusConfig config = new CiStatusConfig();
        config.setRepoUrl("https://jenkins.example.com/");
        config.setRepoName("review-agent-ci");
        config.setStatusContext("Review Agent Gate");
        config.setApiToken("jenkins-token");
        config.setWebhookSecret("codex");

        CiProviderStatusRequest request = factory.build(config, "abc123", "pending", "review running", 42L);
        CiProviderStatusRequest withCrumb = factory.withCrumb(request, new JenkinsCrumb("Jenkins-Crumb", "crumb-value"));

        assertEquals("https://jenkins.example.com/crumbIssuer/api/json", factory.buildCrumbUrl(config));
        assertEquals(request.headers().get("Authorization"), factory.buildCrumbHeaders(config).get("Authorization"));
        assertEquals("crumb-value", withCrumb.headers().get("Jenkins-Crumb"));
        assertEquals(request.url(), withCrumb.url());
    }
}
