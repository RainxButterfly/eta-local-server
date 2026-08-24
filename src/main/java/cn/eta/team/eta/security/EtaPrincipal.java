// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.security;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 认证主体：轻量保存当前登录用户标识，供 Controller / Service 读取当前用户。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public record EtaPrincipal(Long userId, String email) {

    public static EtaPrincipal from() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof EtaPrincipal p ? p : null;
    }
}