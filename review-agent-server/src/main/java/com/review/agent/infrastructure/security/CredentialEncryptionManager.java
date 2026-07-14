package com.review.agent.infrastructure.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class CredentialEncryptionManager {

    public static final String ENVELOPE_PREFIX = "enc:v1:";
    public static final String DEVELOPMENT_KEY = "review-agent-dev-credential-key-change-me";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private static volatile SecretKeySpec activeKey;
    private static volatile List<SecretKeySpec> decryptionKeys = List.of();

    private final String configuredKey;
    private final String configuredPreviousKeys;
    private final SecureRandom secureRandom = new SecureRandom();

    public CredentialEncryptionManager(
            @Value("${review-agent.credentials.master-key:" + DEVELOPMENT_KEY + "}") String configuredKey,
            @Value("${review-agent.credentials.previous-keys:}") String configuredPreviousKeys) {
        this.configuredKey = configuredKey;
        this.configuredPreviousKeys = configuredPreviousKeys;
    }

    @PostConstruct
    public void initialize() {
        activeKey = deriveKey(configuredKey);
        List<SecretKeySpec> keys = new ArrayList<>();
        keys.add(activeKey);
        if (configuredPreviousKeys != null && !configuredPreviousKeys.isBlank()) {
            for (String previousKey : configuredPreviousKeys.split(",")) {
                String candidate = previousKey.trim();
                if (!candidate.isBlank() && !candidate.equals(configuredKey)) {
                    keys.add(deriveKey(candidate));
                }
            }
        }
        decryptionKeys = List.copyOf(keys);
    }

    public String encrypt(String plaintext) {
        return encryptValue(plaintext, secureRandom);
    }

    public String decrypt(String storedValue) {
        return decryptValue(storedValue);
    }

    public String reencrypt(String storedValue) {
        String plaintext = decryptValue(storedValue);
        return encryptPlaintext(plaintext, secureRandom, requireKey());
    }

    public CredentialKeyState inspect(String storedValue) {
        return inspectValue(storedValue);
    }

    public boolean isDevelopmentKey() {
        return DEVELOPMENT_KEY.equals(configuredKey);
    }

    public boolean isConfigured() {
        return configuredKey != null && !configuredKey.isBlank();
    }

    public boolean hasPreviousKeys() {
        return decryptionKeys.size() > 1;
    }

    static String encryptValue(String plaintext, SecureRandom random) {
        if (plaintext == null || plaintext.isBlank() || plaintext.startsWith(ENVELOPE_PREFIX)) {
            return plaintext;
        }
        return encryptPlaintext(plaintext, random, requireKey());
    }

    private static String encryptPlaintext(String plaintext, SecureRandom random, SecretKeySpec key) {
        if (plaintext == null || plaintext.isBlank()) {
            return plaintext;
        }
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] envelope = ByteBuffer.allocate(nonce.length + ciphertext.length)
                    .put(nonce)
                    .put(ciphertext)
                    .array();
            return ENVELOPE_PREFIX + Base64.getEncoder().encodeToString(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encrypt integration credential", ex);
        }
    }

    static String decryptValue(String storedValue) {
        if (storedValue == null || !storedValue.startsWith(ENVELOPE_PREFIX)) {
            return storedValue;
        }
        for (SecretKeySpec key : requireDecryptionKeys()) {
            try {
                return decryptEnvelope(storedValue, key);
            } catch (Exception ignored) {
                // Try the remaining historical keys during a controlled rotation window.
            }
        }
        throw new IllegalStateException(
                "Unable to decrypt integration credential; verify REVIEW_AGENT_CREDENTIAL_KEY and REVIEW_AGENT_CREDENTIAL_PREVIOUS_KEYS");
    }

    static CredentialKeyState inspectValue(String storedValue) {
        if (storedValue == null || storedValue.isBlank() || !storedValue.startsWith(ENVELOPE_PREFIX)) {
            return CredentialKeyState.PLAINTEXT;
        }
        List<SecretKeySpec> keys = requireDecryptionKeys();
        for (int index = 0; index < keys.size(); index++) {
            try {
                decryptEnvelope(storedValue, keys.get(index));
                return index == 0 ? CredentialKeyState.ACTIVE : CredentialKeyState.PREVIOUS;
            } catch (Exception ignored) {
                // GCM authentication failure means this is not the matching key.
            }
        }
        return CredentialKeyState.UNREADABLE;
    }

    private static String decryptEnvelope(String storedValue, SecretKeySpec key) throws Exception {
        try {
            byte[] envelope = Base64.getDecoder().decode(storedValue.substring(ENVELOPE_PREFIX.length()));
            if (envelope.length <= NONCE_LENGTH) {
                throw new IllegalArgumentException("Invalid credential envelope");
            }
            ByteBuffer buffer = ByteBuffer.wrap(envelope);
            byte[] nonce = new byte[NONCE_LENGTH];
            buffer.get(nonce);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static SecretKeySpec requireKey() {
        SecretKeySpec key = activeKey;
        if (key == null) {
            throw new IllegalStateException("Credential encryption key has not been initialized");
        }
        return key;
    }

    private static List<SecretKeySpec> requireDecryptionKeys() {
        List<SecretKeySpec> keys = decryptionKeys;
        if (keys.isEmpty()) {
            throw new IllegalStateException("Credential encryption keys have not been initialized");
        }
        return keys;
    }

    private SecretKeySpec deriveKey(String masterKey) {
        if (masterKey == null || masterKey.isBlank()) {
            throw new IllegalStateException("Credential encryption master key must not be blank");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(masterKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize credential encryption", ex);
        }
    }

    public enum CredentialKeyState {
        ACTIVE,
        PREVIOUS,
        PLAINTEXT,
        UNREADABLE
    }
}
