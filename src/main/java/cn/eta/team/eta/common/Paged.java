// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 统一分页返回结构 { list, total, page, pageSize }，与前端 PageResult 对齐。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public record Paged<T>(List<T> list, long total, long page, long pageSize) {

    public static <T> Paged<T> of(Page<T> page) {
        return new Paged<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    public static <T> Paged<T> of(List<T> list, long total, long page, long pageSize) {
        return new Paged<>(list, total, page, pageSize);
    }
}