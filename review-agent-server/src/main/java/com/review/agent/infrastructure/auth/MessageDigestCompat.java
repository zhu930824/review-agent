package com.review.agent.infrastructure.auth;

import java.security.MessageDigest;

final class MessageDigestCompat {

    private MessageDigestCompat() {
    }

    static boolean isEqual(byte[] left, byte[] right) {
        return MessageDigest.isEqual(left, right);
    }
}
