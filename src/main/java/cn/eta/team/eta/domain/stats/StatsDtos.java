// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.stats;

public final class StatsDtos {

    private StatsDtos() {
    }

    public record OverviewResponse(
            int totalTasks,
            int doneTasks,
            int completionRate,
            int doingTasks,
            double studyHours,
            int totalNotes,
            int pendingErrors,
            int errorMasteryRate) {
    }

    public record ActivityPoint(
            String label,
            double study,
            double tasks) {
    }

    public record SubjectDistribution(
            String label,
            int value,
            String color) {
    }

    public record HeatmapPoint(
            String date,
            int value) {
    }
}
