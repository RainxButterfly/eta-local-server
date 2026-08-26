// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.setting;

import java.util.Map;

public final class SettingDtos {

    private SettingDtos() {
    }

    public record SettingResponse(
            String theme,
            String themeColor,
            Boolean compact,
            Boolean autoLaunch,
            Boolean minimizeToTray,
            String startupPage,
            Boolean reviewReminder,
            Boolean taskDueReminder,
            Boolean weeklyReport,
            String language,
            Map<String, String> shortcuts) {
    }

    public record UpdateRequest(
            String theme,
            String themeColor,
            Boolean compact,
            Boolean autoLaunch,
            Boolean minimizeToTray,
            String startupPage,
            Boolean reviewReminder,
            Boolean taskDueReminder,
            Boolean weeklyReport,
            String language,
            Map<String, String> shortcuts) {
    }
}
