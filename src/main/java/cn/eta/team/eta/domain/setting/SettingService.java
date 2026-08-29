// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.setting;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.domain.setting.SettingDtos.SettingResponse;
import cn.eta.team.eta.domain.setting.SettingDtos.UpdateRequest;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    @Transactional(readOnly = true)
    public SettingResponse get(String ownerId) {
        Setting setting = settingRepository.findByOwnerId(ownerId).orElse(null);
        if (setting == null) {
            return defaultResponse();
        }
        return toResponse(setting);
    }

    @Transactional
    public SettingResponse update(String ownerId, UpdateRequest req) {
        Setting setting = settingRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Setting s = new Setting();
            s.setOwnerId(ownerId);
            s.setShortcuts(JsonUtils.toJson(defaultShortcuts()));
            return s;
        });

        if (req.theme() != null) setting.setTheme(req.theme());
        if (req.themeColor() != null) setting.setThemeColor(req.themeColor());
        if (req.compact() != null) setting.setCompact(req.compact());
        if (req.autoLaunch() != null) setting.setAutoLaunch(req.autoLaunch());
        if (req.minimizeToTray() != null) setting.setMinimizeToTray(req.minimizeToTray());
        if (req.startupPage() != null) setting.setStartupPage(req.startupPage());
        if (req.reviewReminder() != null) setting.setReviewReminder(req.reviewReminder());
        if (req.taskDueReminder() != null) setting.setTaskDueReminder(req.taskDueReminder());
        if (req.weeklyReport() != null) setting.setWeeklyReport(req.weeklyReport());
        if (req.language() != null) setting.setLanguage(req.language());
        if (req.shortcuts() != null) setting.setShortcuts(JsonUtils.toJson(req.shortcuts()));

        setting.setUpdatedAt(Instant.now());
        settingRepository.save(setting);
        return toResponse(setting);
    }

    private SettingResponse toResponse(Setting s) {
        Map<String, String> shortcuts = parseShortcuts(s.getShortcuts());
        return new SettingResponse(
                s.getTheme(), s.getThemeColor(), s.getCompact(), s.getAutoLaunch(),
                s.getMinimizeToTray(), s.getStartupPage(), s.getReviewReminder(),
                s.getTaskDueReminder(), s.getWeeklyReport(), s.getLanguage(), shortcuts);
    }

    private SettingResponse defaultResponse() {
        return new SettingResponse(
                "light", "#7c6cf0", false, false, true, "home",
                true, true, false, "zh", defaultShortcuts());
    }

    private Map<String, String> defaultShortcuts() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("search", "Ctrl+K");
        m.put("newTask", "Ctrl+N");
        m.put("newNote", "Ctrl+Shift+N");
        m.put("newError", "Ctrl+E");
        m.put("toggleTheme", "Ctrl+Shift+L");
        return m;
    }

    private Map<String, String> parseShortcuts(String json) {
        if (json == null || json.isBlank()) {
            return defaultShortcuts();
        }
        Map<String, String> parsed = JsonUtils.fromJson(json, new TypeReference<Map<String, String>>() {});
        return parsed != null ? parsed : defaultShortcuts();
    }
}
