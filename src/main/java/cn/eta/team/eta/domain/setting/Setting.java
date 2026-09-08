// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.setting;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eta_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Setting {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    private String theme = "light";

    @Column(name = "theme_color")
    private String themeColor = "#7c6cf0";

    private Boolean compact = false;

    @Column(name = "auto_launch")
    private Boolean autoLaunch = false;

    @Column(name = "minimize_to_tray")
    private Boolean minimizeToTray = true;

    @Column(name = "startup_page")
    private String startupPage = "home";

    @Column(name = "review_reminder")
    private Boolean reviewReminder = true;

    @Column(name = "task_due_reminder")
    private Boolean taskDueReminder = true;

    @Column(name = "weekly_report")
    private Boolean weeklyReport = false;

    private String language = "zh";

    @Lob
    private String shortcuts;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted = false;
}
