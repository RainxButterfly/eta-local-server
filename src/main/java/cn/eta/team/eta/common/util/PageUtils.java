// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common.util;

/**
 * 分页工具：统一 page / pageSize 的归一化与边界防护。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class PageUtils {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageUtils() {
    }

    /** 归一化页码：小于 0 视为 0。 */
    public static int page(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    /** 归一化页大小：空或非法使用默认值。 */
    public static int size(Integer pageSize) {
        return normalize(pageSize, DEFAULT_SIZE, MAX_SIZE);
    }

    /**
     * 归一化大小：小于等于 0 用默认值，超过上限收敛到上限。
     */
    public static int normalize(Integer value, int defaultValue, int max) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return Math.min(value, max);
    }
}