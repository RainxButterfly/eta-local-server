// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.eta.team.eta.domain.plugin.PluginDtos.MarketPlugin;
import cn.eta.team.eta.domain.plugin.PluginDtos.Plugin;
import cn.eta.team.eta.domain.plugin.PluginDtos.ToggleResult;

@Service
public class PluginService {

    private final Map<String, Boolean> enabledState = new ConcurrentHashMap<>();

    private final List<Plugin> installedPlugins = List.of(
            new Plugin("p-pomodoro", "番茄钟", "专注计时 + 自定义时长", "ETA",
                    true, true, "clock", "red", "1.2k", 4.8),
            new Plugin("p-white-noise", "白噪音", "环境音效助专注", "ETA",
                    true, false, "volume", "blue", "890", 4.5));

    private final List<MarketPlugin> marketPlugins = List.of(
            new MarketPlugin("p-spaced-repetition", "间隔复习增强", "SM-2 算法升级", "Community",
                    false, 4.7, "2.1k"),
            new MarketPlugin("p-data-visualizer", "数据可视化", "学习图表仪表盘", "Community",
                    false, 4.6, "1.5k"),
            new MarketPlugin("p-theme-editor", "主题编辑器", "自定义配色方案", "ETA",
                    false, 4.4, "980"));

    public List<Plugin> list() {
        return installedPlugins.stream()
                .map(p -> new Plugin(p.id(), p.name(), p.desc(), p.author(), p.installed(),
                        enabledState.getOrDefault(p.id(), p.enabled()),
                        p.icon(), p.tone(), p.downloads(), p.rating()))
                .toList();
    }

    public List<MarketPlugin> market() {
        return marketPlugins;
    }

    public ToggleResult toggle(String id, boolean enabled) {
        enabledState.put(id, enabled);
        return new ToggleResult(id, enabled);
    }
}
