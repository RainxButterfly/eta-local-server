// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 加密与随机数工具：散列 / Base64 / BCrypt / 随机码生成。
 * <p>
 * 密码保存请使用 {@link #bcrypt(String)}（不可逆、自带盐）；
 * MD5 / SHA-256 仅用于完整性校验等非安全场景。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class CryptUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private CryptUtils() {
    }

    /** BCrypt 加密（默认强度）。 */
    public static String bcrypt(String raw) {
        return ENCODER.encode(raw);
    }

    /** 校验明文是否匹配 BCrypt 密文。 */
    public static boolean bcryptMatches(String raw, String hashed) {
        return ENCODER.matches(raw, hashed);
    }

    /** MD5（十六进制小写）。 */
    public static String md5(String text) {
        return digest("MD5", text);
    }

    /** SHA-256（十六进制小写）。 */
    public static String sha256(String text) {
        return digest("SHA-256", text);
    }

    private static String digest(String algorithm, String text) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(md.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("不支持的摘要算法: " + algorithm, e);
        }
    }

    /** Base64 编码。 */
    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /** Base64 解码。 */
    public static String base64Decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }

    /** 生成纯数字验证码（如短信验证码）。 */
    public static String randomVerifyCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /** 生成指定长度的随机字母数字串。 */
    public static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }
        return sb.toString();
    }

    /** 生成固定 6 位数字验证码。 */
    public static String verifyCode6() {
        return randomVerifyCode(6);
    }
}