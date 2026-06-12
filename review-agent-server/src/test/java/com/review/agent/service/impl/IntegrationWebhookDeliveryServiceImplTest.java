package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.infrastructure.webhook.GitHubWebhookSignatureVerifier;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationWebhookDeliveryServiceImplTest {

    private final FakeDeliveryRepository deliveryRepository = new FakeDeliveryRepository();
    private final IntegrationWebhookDeliveryServiceImpl service = new IntegrationWebhookDeliveryServiceImpl(
            deliveryRepository,
            new GitHubWebhookSignatureVerifier());

    @Test
    void recordsAcceptedGitHubDeliveryWhenSignatureMatches() throws Exception {
        deliveryRepository.config = config("secret");
        String payload = "{\"action\":\"opened\"}";

        IntegrationWebhookDeliveryResultVO result = service.receiveGitHubDelivery(
                "delivery-1",
                "pull_request",
                signature("secret", payload),
                payload);

        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("delivery-1", result.getDeliveryId());
        assertEquals("pull_request", result.getEventType());
        assertEquals(false, result.getDuplicate());
        assertEquals("ACCEPTED", deliveryRepository.saved.get(0).getDeliveryStatus());
        assertEquals("github-checks", deliveryRepository.saved.get(0).getConnectorKey());
        assertEquals(64, deliveryRepository.saved.get(0).getPayloadDigest().length());
    }

    @Test
    void rejectsGitHubDeliveryWhenSignatureDoesNotMatch() {
        deliveryRepository.config = config("secret");

        assertThrows(IllegalArgumentException.class, () -> service.receiveGitHubDelivery(
                "delivery-2",
                "pull_request",
                "sha256=bad",
                "{}"));

        assertEquals("REJECTED", deliveryRepository.saved.get(0).getDeliveryStatus());
        assertEquals("Invalid GitHub webhook signature", deliveryRepository.saved.get(0).getErrorMessage());
    }

    @Test
    void treatsRepeatedDeliveryIdAsDuplicate() throws Exception {
        deliveryRepository.config = config("secret");
        IntegrationWebhookDeliveryLog existing = new IntegrationWebhookDeliveryLog();
        existing.setDeliveryId("delivery-3");
        existing.setEventType("pull_request");
        existing.setDeliveryStatus("ACCEPTED");
        deliveryRepository.existing = existing;

        IntegrationWebhookDeliveryResultVO result = service.receiveGitHubDelivery(
                "delivery-3",
                "pull_request",
                signature("secret", "{}"),
                "{}");

        assertEquals("DUPLICATE", result.getStatus());
        assertEquals(true, result.getDuplicate());
        assertTrue(deliveryRepository.saved.isEmpty());
    }

    private CiStatusConfig config(String secret) {
        CiStatusConfig config = new CiStatusConfig();
        config.setConnectorKey("github-checks");
        config.setProvider("GITHUB");
        config.setWebhookSecret(secret);
        return config;
    }

    private String signature(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private static class FakeDeliveryRepository implements IntegrationWebhookDeliveryRepository {
        private CiStatusConfig config;
        private IntegrationWebhookDeliveryLog existing;
        private final List<IntegrationWebhookDeliveryLog> saved = new ArrayList<>();

        @Override
        public Optional<CiStatusConfig> findConfig(String connectorKey) {
            return Optional.ofNullable(config);
        }

        @Override
        public Optional<IntegrationWebhookDeliveryLog> findByDeliveryId(String connectorKey, String deliveryId) {
            return Optional.ofNullable(existing);
        }

        @Override
        public void save(IntegrationWebhookDeliveryLog log) {
            saved.add(log);
        }
    }
}
