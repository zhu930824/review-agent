package com.review.agent.common.util;

import cn.sdh.intelligent.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final String AES = "AES";

    @Value("${jwt.secret}")
    private String secret;

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey());
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BizException(500, "加密数据源密码失败", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, buildKey());
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException(500, "解密数据源密码失败", e);
        }
    }

    private SecretKeySpec buildKey() {
        byte[] bytes = new byte[16];
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, bytes, 0, Math.min(secretBytes.length, bytes.length));
        return new SecretKeySpec(bytes, AES);
    }
}
