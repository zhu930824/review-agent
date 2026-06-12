package com.review.agent.infrastructure.webhook;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubWebhookSignatureVerifierTest {

    private final GitHubWebhookSignatureVerifier verifier = new GitHubWebhookSignatureVerifier();

    @Test
    void acceptsGitHubSha256Signature() throws Exception {
        String payload = "{\"action\":\"opened\"}";
        String signature = "sha256=" + hmacSha256("secret", payload);

        assertTrue(verifier.isValid("secret", payload, signature));
    }

    @Test
    void rejectsMissingOrWrongSignature() {
        String payload = "{\"action\":\"opened\"}";

        assertFalse(verifier.isValid("secret", payload, null));
        assertFalse(verifier.isValid("secret", payload, "sha256=bad"));
        assertFalse(verifier.isValid("", payload, "sha256=bad"));
    }

    private String hmacSha256(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
