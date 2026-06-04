package com.review.agent.infrastructure.ci;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpCiStatusPublisherTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsStatusPayloadToConfiguredEndpointWithBearerToken() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                true,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/status",
                "ci-token",
                "generic",
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked",
                "https://review-agent.local/reviews/42"));

        assertEquals("Bearer ci-token", authHeader.get());
        assertTrue(body.get().contains("\"reviewId\":42"));
        assertTrue(body.get().contains("\"state\":\"FAILURE\""));
        assertTrue(body.get().contains("\"context\":\"review-agent/pre-pr\""));
        assertTrue(body.get().contains("\"targetUrl\":\"https://review-agent.local/reviews/42\""));
    }

    @Test
    void skipsPublishWhenDisabled() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                false,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/status",
                "ci-token",
                "generic",
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(1L, CiStatusState.SUCCESS, "ctx", "ok", ""));

        assertEquals("", body.get());
        assertEquals("", authHeader.get());
    }

    @Test
    void postsGitHubSpecificPayloadWhenProviderIsGitHub() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                true,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/status",
                "",
                "github",
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked",
                "https://review-agent.local/reviews/42"));

        assertTrue(body.get().contains("\"state\":\"failure\""));
        assertTrue(body.get().contains("\"context\":\"review-agent/pre-pr\""));
        assertTrue(body.get().contains("\"target_url\":\"https://review-agent.local/reviews/42\""));
    }

    @Test
    void postsGitHubStatusToDerivedCommitStatusEndpointWhenRepoMetadataExists() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                true,
                "",
                "",
                "github",
                "http://127.0.0.1:" + server.getAddress().getPort(),
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(
                42L,
                CiStatusState.SUCCESS,
                "review-agent/pre-pr",
                "passed",
                "https://review-agent.local/reviews/42",
                "https://github.com/acme/review-agent.git",
                "abc123"));

        assertTrue(body.get().contains("\"state\":\"success\""));
        assertEquals("/repos/acme/review-agent/statuses/abc123", authHeader.get());
    }

    @Test
    void postsGitLabSpecificPayloadWhenProviderIsGitLab() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                true,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/status",
                "",
                "gitlab",
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked",
                "https://review-agent.local/reviews/42"));

        assertTrue(body.get().contains("\"state\":\"failed\""));
        assertTrue(body.get().contains("\"name\":\"review-agent/pre-pr\""));
        assertTrue(body.get().contains("\"target_url\":\"https://review-agent.local/reviews/42\""));
    }

    @Test
    void postsGitLabStatusToDerivedCommitStatusEndpointWhenRepoMetadataExists() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpCiStatusPublisher publisher = new HttpCiStatusPublisher(
                true,
                "",
                "",
                "gitlab",
                "http://127.0.0.1:" + server.getAddress().getPort(),
                new CiProviderPayloadFactory());

        publisher.publish(new CiStatusPayload(
                42L,
                CiStatusState.FAILURE,
                "review-agent/pre-pr",
                "blocked",
                "https://review-agent.local/reviews/42",
                "https://gitlab.example.com/platform/tools/review-agent.git",
                "abc123"));

        assertTrue(body.get().contains("\"state\":\"failed\""));
        assertEquals("/projects/platform%2Ftools%2Freview-agent/statuses/abc123", authHeader.get());
    }

    private static HttpServer startServer(AtomicReference<String> body, AtomicReference<String> authHeader)
            throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/status", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.createContext("/repos/acme/review-agent/statuses/abc123", exchange -> {
            authHeader.set(exchange.getRequestURI().getPath());
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.createContext("/projects/platform/tools/review-agent/statuses/abc123", exchange -> {
            authHeader.set(exchange.getRequestURI().getRawPath());
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        return server;
    }
}
