package com.review.agent.common.util;

import cn.hutool.crypto.digest.DigestUtil;

public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256(String raw) {
        return DigestUtil.sha256Hex(raw);
    }

    public static String md5(String raw) {
        return DigestUtil.md5Hex(raw);
    }
}
