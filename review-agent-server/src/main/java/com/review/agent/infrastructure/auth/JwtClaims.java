package com.review.agent.infrastructure.auth;

public record JwtClaims(Long userId, String username, long expiresAt) {
}
