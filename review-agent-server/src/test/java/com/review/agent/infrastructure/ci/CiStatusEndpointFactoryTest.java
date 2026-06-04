package com.review.agent.infrastructure.ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiStatusEndpointFactoryTest {

    @Test
    void buildsGitHubCommitStatusEndpointFromHttpsRepositoryUrl() {
        CiStatusEndpointFactory factory = new CiStatusEndpointFactory("https://api.github.com");
        CiStatusEndpoint endpoint = factory.github(
                "https://github.com/acme/review-agent.git",
                "abc123");

        assertEquals(
                "https://api.github.com/repos/acme/review-agent/statuses/abc123",
                endpoint.url());
    }

    @Test
    void buildsGitLabCommitStatusEndpointFromSshRepositoryUrl() {
        CiStatusEndpointFactory factory = new CiStatusEndpointFactory("https://gitlab.example.com/api/v4");
        CiStatusEndpoint endpoint = factory.gitlab(
                "git@gitlab.example.com:platform/review-agent.git",
                "abc123");

        assertEquals(
                "https://gitlab.example.com/api/v4/projects/platform%2Freview-agent/statuses/abc123",
                endpoint.url());
    }

    @Test
    void buildsGitLabCommitStatusEndpointForNestedGroupRepositoryUrl() {
        CiStatusEndpointFactory factory = new CiStatusEndpointFactory("https://gitlab.example.com/api/v4");
        CiStatusEndpoint endpoint = factory.gitlab(
                "https://gitlab.example.com/platform/tools/review-agent.git",
                "abc123");

        assertEquals(
                "https://gitlab.example.com/api/v4/projects/platform%2Ftools%2Freview-agent/statuses/abc123",
                endpoint.url());
    }

    @Test
    void keepsStaticEndpointWhenProviderIsGeneric() {
        CiStatusEndpointFactory factory = new CiStatusEndpointFactory("");
        CiStatusEndpoint endpoint = factory.generic("https://ci.example.com/status");

        assertEquals("https://ci.example.com/status", endpoint.url());
    }
}
