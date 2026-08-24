// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 全局设置（TODO：持久化到数据库配置表）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/settings")
public class SettingsController {

    @GetMapping
    public Result<Map<String, Object>> get() {
        return Result.ok(Map.ofEntries(
                Map.entry("theme", "light"),
                Map.entry("themeColor", "#7c6cf0"),
                Map.entry("compact", false),
                Map.entry("autoLaunch", false),
                Map.entry("minimizeToTray", true),
                Map.entry("startupPage", "home"),
                Map.entry("reviewReminder", true),
                Map.entry("taskDueReminder", true),
                Map.entry("weeklyReport", false),
                Map.entry("language", "zh"),
                Map.entry("shortcuts", Map.of(
                        "search", "Ctrl+K",
                        "newTask", "Ctrl+N",
                        "newNote", "Ctrl+Shift+N",
                        "newError", "Ctrl+E",
                        "toggleTheme", "Ctrl+Shift+L"))));
    }

    @PutMapping
    public Result<Map<String, Object>> update(@RequestBody Map<String, Object> body) {
        return Result.ok(body);
    }
}