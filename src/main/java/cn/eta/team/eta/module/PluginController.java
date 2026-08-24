// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 插件系统（TODO：实现插件协议与沙箱加载）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/plugins")
public class PluginController {

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(List.of(
                Map.of("id", "p-pomodoro", "name", "番茄钟", "desc", "专注计时 + 自定义时长",
                        "author", "ETA", "installed", true, "enabled", true, "icon", "clock",
                        "tone", "red", "downloads", "1.2k", "rating", 4.8)));
    }

    @GetMapping("/market")
    public Result<List<Map<String, Object>>> market() {
        return Result.ok(List.of(
                Map.of("id", "p-pomodoro", "name", "番茄钟", "desc", "专注计时 + 自定义时长",
                        "author", "ETA", "installed", true, "enabled", true, "icon", "clock",
                        "tone", "red", "downloads", "1.2k", "rating", 4.8)));
    }

    @PostMapping("/{id}/toggle")
    public Result<Map<String, Boolean>> toggle(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        return Result.ok(Map.of("enabled", body.getOrDefault("enabled", true)));
    }
}