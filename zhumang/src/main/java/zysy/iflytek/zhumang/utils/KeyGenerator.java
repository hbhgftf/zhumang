package zysy.iflytek.zhumang.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // 生成 64 字节的随机密钥（满足 HS512 要求，512 位）
        byte[] secretBytes = new byte[64];
        new SecureRandom().nextBytes(secretBytes);
        // 生成 URL 安全的 Base64 编码（无填充符 =）
        String urlSafeSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        System.out.println("jwt.secret=" + urlSafeSecret);
    }
}