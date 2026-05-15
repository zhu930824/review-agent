package com.review.agent.infrastructure.auth;

public final class AuthContext {

    private static final ThreadLocal<JwtClaims> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(JwtClaims claims) {
        CURRENT.set(claims);
    }

    public static JwtClaims get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
