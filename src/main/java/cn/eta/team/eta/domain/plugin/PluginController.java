// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.plugin;

import java.util.List;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.plugin.PluginDtos.MarketPlugin;
import cn.eta.team.eta.domain.plugin.PluginDtos.Plugin;
import cn.eta.team.eta.domain.plugin.PluginDtos.ToggleRequest;
import cn.eta.team.eta.domain.plugin.PluginDtos.ToggleResult;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class PluginController {

    private final PluginService pluginService;

    @GetMapping
    public Result<List<Plugin>> list() {
        return Result.ok(pluginService.list());
    }

    @GetMapping("/market")
    public Result<List<MarketPlugin>> market() {
        return Result.ok(pluginService.market());
    }

    @PostMapping("/{id}/toggle")
    public Result<ToggleResult> toggle(@PathVariable String id,
                                        @RequestBody ToggleRequest req) {
        return Result.ok(pluginService.toggle(id, req.enabled()));
    }
}
