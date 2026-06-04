package com.review.agent.infrastructure.integration;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpPrePrReportPublisherTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsMarkdownReportToConfiguredEndpointWithBearerToken() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpPrePrReportPublisher publisher = new HttpPrePrReportPublisher(
                true,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/comments",
                "report-token");

        boolean published = publisher.publish(42L, "# Review Agent Pre-PR 审查报告");

        assertTrue(published);
        assertEquals("Bearer report-token", authHeader.get());
        assertTrue(body.get().contains("\"reviewId\":42"));
        assertTrue(body.get().contains("\"format\":\"MARKDOWN\""));
        assertTrue(body.get().contains("Review Agent Pre-PR"));
    }

    @Test
    void skipsPublishWhenDisabled() throws Exception {
        AtomicReference<String> body = new AtomicReference<>("");
        AtomicReference<String> authHeader = new AtomicReference<>("");
        server = startServer(body, authHeader);

        HttpPrePrReportPublisher publisher = new HttpPrePrReportPublisher(
                false,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/comments",
                "report-token");

        boolean published = publisher.publish(42L, "# report");

        assertEquals(false, published);
        assertEquals("", body.get());
        assertEquals("", authHeader.get());
    }

    private static HttpServer startServer(AtomicReference<String> body, AtomicReference<String> authHeader)
            throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/comments", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        return server;
    }
}
