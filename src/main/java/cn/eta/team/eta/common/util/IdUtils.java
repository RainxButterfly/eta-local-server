package cn.eta.team.eta.common.util;

import java.util.UUID;

/**
 * ID 生成工具：UUID / 短随机字串。
 */
public final class IdUtils {

    private IdUtils() {
    }

    /** 32 位无横线的 UUID。 */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 36 位标准 UUID（带横线）。 */
    public static String uuid36() {
        return UUID.randomUUID().toString();
    }

    /** 默认长度的随机字母数字串（用于短 ID / 文件前缀）。 */
    public static String randomString() {
        return CryptUtils.randomAlphanumeric(16);
    }

    /** 生成 n 位随机字母数字串。 */
    public static String randomString(int length) {
        return CryptUtils.randomAlphanumeric(length);
    }

    /** 生成 n 位纯数字串。 */
    public static String randomNumber(int length) {
        return CryptUtils.randomVerifyCode(length);
    }
}