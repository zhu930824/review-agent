package com.review.agent.infrastructure.ci;

import org.springframework.stereotype.Component;

@Component
public class CiProviderPayloadFactory {

    public GitHubStatusPayload toGitHub(CiStatusPayload payload) {
        return new GitHubStatusPayload(
                toGitHubState(payload.state()),
                payload.context(),
                payload.description(),
                payload.targetUrl());
    }

    public GitLabStatusPayload toGitLab(CiStatusPayload payload) {
        return new GitLabStatusPayload(
                toGitLabState(payload.state()),
                payload.context(),
                payload.description(),
                payload.targetUrl());
    }

    private String toGitHubState(CiStatusState state) {
        return switch (state) {
            case SUCCESS -> "success";
            case FAILURE -> "failure";
            case PENDING -> "pending";
        };
    }

    private String toGitLabState(CiStatusState state) {
        return switch (state) {
            case SUCCESS -> "success";
            case FAILURE -> "failed";
            case PENDING -> "pending";
        };
    }
}
