// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.plugin;

public final class PluginDtos {

    private PluginDtos() {
    }

    public record Plugin(
            String id,
            String name,
            String desc,
            String author,
            boolean installed,
            boolean enabled,
            String icon,
            String tone,
            String downloads,
            double rating) {
    }

    public record MarketPlugin(
            String id,
            String name,
            String desc,
            String author,
            boolean installed,
            double rating,
            String downloads) {
    }

    public record ToggleRequest(boolean enabled) {
    }

    public record ToggleResult(String id, boolean enabled) {
    }
}
