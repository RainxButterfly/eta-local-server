// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.search;

import java.util.List;

public final class SearchDtos {

    private SearchDtos() {
    }

    public record SearchItem(
            String id,
            String type,
            String title,
            String subtitle,
            String tone) {
    }

    public record SearchGroup(
            String type,
            String name,
            List<SearchItem> items) {
    }

    public record SearchResponse(
            String keyword,
            long total,
            List<SearchGroup> groups) {
    }
}
