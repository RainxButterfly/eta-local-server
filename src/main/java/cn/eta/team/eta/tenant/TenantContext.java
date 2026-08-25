// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.tenant;

/**
 * 线程级租户上下文：保存当前请求所属用户 ID，供动态数据源路由使用。
 * 请求结束时必须调用 {@link #clear()} 清理，避免线程池复用导致串数据。
 */
import java.util.function.Supplier;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String userId) {
        CURRENT.set(userId);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static <T> T runAsDefault(Supplier<T> action) {
        String previous = CURRENT.get();
        CURRENT.remove();
        try {
            return action.get();
        } finally {
            if (previous != null) {
                CURRENT.set(previous);
            }
        }
    }
}
