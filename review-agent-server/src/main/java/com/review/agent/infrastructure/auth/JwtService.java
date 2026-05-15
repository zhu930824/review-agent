package com.review.agent.infrastructure.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.common.exception.BizException;
import com.review.agent.domain.exception.CommonExceptionEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expireSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${review-agent.auth.jwt-secret}") String secret,
            @Value("${review-agent.auth.jwt-expire-hours:168}") long expireHours) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expireSeconds = expireHours * 3600;
    }

    public TokenResult createToken(Long userId, String username) {
        long expiresAt = Instant.now().getEpochSecond() + expireSeconds;
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", userId);
            payload.put("username", username);
            payload.put("exp", expiresAt);

            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signature = sign(headerPart + "." + payloadPart);
            return new TokenResult(headerPart + "." + payloadPart + "." + signature, expiresAt);
        } catch (Exception e) {
            throw new BizException(CommonExceptionEnum.INTERNAL_ERROR, e);
        }
    }

    public JwtClaims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BizException(CommonExceptionEnum.AUTH_TOKEN_INVALID);
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new BizException(CommonExceptionEnum.AUTH_TOKEN_INVALID);
            }

            Map<String, Object> payload = objectMapper.readValue(base64UrlDecode(parts[1]), MAP_TYPE);
            long expiresAt = toLong(payload.get("exp"));
            if (expiresAt <= Instant.now().getEpochSecond()) {
                throw new BizException(CommonExceptionEnum.AUTH_TOKEN_EXPIRED);
            }

            return new JwtClaims(toLong(payload.get("sub")), String.valueOf(payload.get("username")), expiresAt);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(CommonExceptionEnum.AUTH_TOKEN_INVALID, e);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return base64UrlEncode(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestCompat.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }

    public record TokenResult(String token, Long expiresAt) {
    }
}
