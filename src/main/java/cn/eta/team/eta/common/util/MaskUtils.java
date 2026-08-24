// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common.util;

/**
 * 脱敏工具：用于日志或展示时不泄露完整敏感信息（邮箱 / 手机号 / 姓名）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class MaskUtils {

    private MaskUtils() {
    }

    /** 邮箱脱敏：abc@x.com -> a**@x.com。 */
    public static String email(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email.charAt(0) + "***@" + email.substring(Math.min(at + 1, email.length()));
        }
        return email.substring(0, 1) + "**" + email.substring(at);
    }

    /** 手机号脱敏：13800001111 -> 138****1111。 */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 姓名脱敏：张三 -> 张*；欧阳娜娜 -> 欧**娜。 */
    public static String name(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        int len = name.length();
        if (len == 1) {
            return name;
        }
        if (len == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(len - 2) + name.charAt(len - 1);
    }
}