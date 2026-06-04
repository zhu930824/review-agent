package com.review.agent.infrastructure.ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiProviderPayloadFactoryTest {

    @Test
    void convertsProviderNeutralPayloadToGitHubCommitStatusPayload() {
        CiProviderPayloadFactory factory = new CiProviderPayloadFactory();
        CiStatusPayload payload = new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked by review",
                "https://review-agent.local/reviews/42");

        GitHubStatusPayload github = factory.toGitHub(payload);

        assertEquals("failure", github.state());
        assertEquals("review-agent/pre-pr", github.context());
        assertEquals("blocked by review", github.description());
        assertEquals("https://review-agent.local/reviews/42", github.targetUrl());
    }

    @Test
    void convertsProviderNeutralPayloadToGitLabCommitStatusPayload() {
        CiProviderPayloadFactory factory = new CiProviderPayloadFactory();
        CiStatusPayload payload = new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked by review",
                "https://review-agent.local/reviews/42");

        GitLabStatusPayload gitlab = factory.toGitLab(payload);

        assertEquals("failed", gitlab.state());
        assertEquals("review-agent/pre-pr", gitlab.name());
        assertEquals("blocked by review", gitlab.description());
        assertEquals("https://review-agent.local/reviews/42", gitlab.targetUrl());
    }

    @Test
    void mapsSuccessAndPendingForBothProviders() {
        CiProviderPayloadFactory factory = new CiProviderPayloadFactory();

        assertEquals("success", factory.toGitHub(payload(CiStatusState.SUCCESS)).state());
        assertEquals("pending", factory.toGitHub(payload(CiStatusState.PENDING)).state());
        assertEquals("success", factory.toGitLab(payload(CiStatusState.SUCCESS)).state());
        assertEquals("pending", factory.toGitLab(payload(CiStatusState.PENDING)).state());
    }

    private static CiStatusPayload payload(CiStatusState state) {
        return new CiStatusPayload(1L, state, "review-agent/pre-pr", "status", "");
    }
}
