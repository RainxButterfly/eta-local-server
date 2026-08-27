// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.stats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.domain.error.ErrorRepository;
import cn.eta.team.eta.domain.note.NoteRepository;
import cn.eta.team.eta.domain.stats.StatsDtos.ActivityPoint;
import cn.eta.team.eta.domain.stats.StatsDtos.HeatmapPoint;
import cn.eta.team.eta.domain.stats.StatsDtos.OverviewResponse;
import cn.eta.team.eta.domain.stats.StatsDtos.SubjectDistribution;
import cn.eta.team.eta.domain.task.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String[] SUBJECT_COLORS = {
            "#7c6cf0", "#2fa47c", "#f59e0b", "#ef4444", "#3b82f6",
            "#06b6d4", "#ec4899", "#8b5cf6", "#10b981", "#f97316"
    };

    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final ErrorRepository errorRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public OverviewResponse overview(String ownerId) {
        int totalTasks = (int) taskRepository.countByOwnerId(ownerId);
        int doneTasks = (int) taskRepository.countByStatusAndOwnerId("done", ownerId);
        int doingTasks = (int) taskRepository.countByStatusAndOwnerId("doing", ownerId);
        int completionRate = totalTasks > 0 ? (doneTasks * 100 / totalTasks) : 0;
        double studyHours = Math.round(doneTasks * 1.5 * 10.0) / 10.0;

        int totalNotes = (int) noteRepository.countByOwnerId(ownerId);

        int totalErrors = (int) errorRepository.countByOwnerId(ownerId);
        int masteredErrors = (int) errorRepository.countByMasteredAndOwnerId(true, ownerId);
        int pendingErrors = totalErrors - masteredErrors;
        int errorMasteryRate = totalErrors > 0 ? (masteredErrors * 100 / totalErrors) : 0;

        return new OverviewResponse(totalTasks, doneTasks, completionRate, doingTasks,
                studyHours, totalNotes, pendingErrors, errorMasteryRate);
    }

    @Transactional(readOnly = true)
    public List<ActivityPoint> activity(String ownerId, String range) {
        int days = switch (range) {
            case "m" -> 30;
            case "q" -> 90;
            default -> 7;
        };

        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> taskCounts = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            taskCounts.put(today.minusDays(i), 0);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                        "SELECT CAST(created_at AS DATE) as d, COUNT(*) FROM eta_task " +
                                "WHERE owner_id = ? AND created_at >= ? GROUP BY CAST(created_at AS DATE)")
                .setParameter(1, ownerId)
                .setParameter(2, today.minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant())
                .getResultList();

        for (Object[] row : rows) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            if (taskCounts.containsKey(date)) {
                taskCounts.put(date, ((Number) row[1]).intValue());
            }
        }

        List<ActivityPoint> points = new ArrayList<>();
        String[] weekdayLabels = {"日", "一", "二", "三", "四", "五", "六"};
        for (Map.Entry<LocalDate, Integer> entry : taskCounts.entrySet()) {
            String label;
            if (days <= 7) {
                label = weekdayLabels[entry.getKey().getDayOfWeek().getValue() % 7];
            } else {
                label = entry.getKey().format(DateTimeFormatter.ofPattern("MM-dd"));
            }
            int taskCount = entry.getValue();
            double study = Math.round(taskCount * 1.5 * 10.0) / 10.0;
            points.add(new ActivityPoint(label, study, taskCount));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<SubjectDistribution> subjectDistribution(String ownerId) {
        List<Object[]> rows = errorRepository.countBySubject(ownerId);
        List<SubjectDistribution> result = new ArrayList<>();
        int i = 0;
        for (Object[] row : rows) {
            String subject = (String) row[0];
            if (subject == null || subject.isBlank()) continue;
            int count = ((Number) row[1]).intValue();
            String color = SUBJECT_COLORS[i % SUBJECT_COLORS.length];
            result.add(new SubjectDistribution(subject, count, color));
            i++;
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<HeatmapPoint> heatmap(String ownerId, String range) {
        int days = switch (range) {
            case "m" -> 30;
            case "q" -> 90;
            default -> 7;
        };

        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> counts = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            counts.put(today.minusDays(i), 0);
        }

        Instant from = today.minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant();

        aggregateDate(counts, "eta_task", ownerId, from);
        aggregateDate(counts, "eta_note", ownerId, from);
        aggregateDate(counts, "eta_error", ownerId, from);

        List<HeatmapPoint> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : counts.entrySet()) {
            result.add(new HeatmapPoint(entry.getKey().toString(), entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void aggregateDate(Map<LocalDate, Integer> counts, String table, String ownerId, Instant from) {
        List<Object[]> rows = entityManager.createNativeQuery(
                        "SELECT CAST(created_at AS DATE) as d, COUNT(*) FROM " + table +
                                " WHERE owner_id = ? AND created_at >= ? GROUP BY CAST(created_at AS DATE)")
                .setParameter(1, ownerId)
                .setParameter(2, from)
                .getResultList();
        for (Object[] row : rows) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            if (counts.containsKey(date)) {
                counts.merge(date, ((Number) row[1]).intValue(), Integer::sum);
            }
        }
    }
}
