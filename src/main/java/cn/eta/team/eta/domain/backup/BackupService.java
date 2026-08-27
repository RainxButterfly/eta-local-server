// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.auth.UserProfile;
import cn.eta.team.eta.auth.UserProfileRepository;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.domain.backup.BackupDtos.BackupInfo;
import cn.eta.team.eta.domain.backup.BackupDtos.ExportResult;
import cn.eta.team.eta.domain.backup.BackupDtos.ImportResult;
import cn.eta.team.eta.domain.error.Error;
import cn.eta.team.eta.domain.error.ErrorRepository;
import cn.eta.team.eta.domain.note.Note;
import cn.eta.team.eta.domain.note.NoteRepository;
import cn.eta.team.eta.domain.note.NoteTag;
import cn.eta.team.eta.domain.note.NoteTagRepository;
import cn.eta.team.eta.domain.resume.Resume;
import cn.eta.team.eta.domain.resume.ResumeRepository;
import cn.eta.team.eta.domain.setting.Setting;
import cn.eta.team.eta.domain.setting.SettingRepository;
import cn.eta.team.eta.domain.task.Task;
import cn.eta.team.eta.domain.task.TaskCategory;
import cn.eta.team.eta.domain.task.TaskCategoryRepository;
import cn.eta.team.eta.domain.task.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class BackupService {

    private static final String BACKUP_DIR = "./data/backups";

    private record TableConfig(String tableName, Class<?> entityClass) {
    }

    private static final List<TableConfig> TABLES = List.of(
            new TableConfig("note_tag", NoteTag.class),
            new TableConfig("eta_task_category", TaskCategory.class),
            new TableConfig("eta_user_profile", UserProfile.class),
            new TableConfig("eta_task", Task.class),
            new TableConfig("eta_note", Note.class),
            new TableConfig("eta_error", Error.class),
            new TableConfig("eta_resume", Resume.class),
            new TableConfig("eta_setting", Setting.class)
    );

    private final TaskRepository taskRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final ErrorRepository errorRepository;
    private final ResumeRepository resumeRepository;
    private final UserProfileRepository userProfileRepository;
    private final SettingRepository settingRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public BackupInfo create(String ownerId, String location) {
        Map<String, List<Object>> snapshot = buildSnapshot(ownerId);
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
        String filename = "eta-backup-" + timestamp + ".json";
        Path dir = Paths.get(BACKUP_DIR, ownerId);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(filename);
            Files.writeString(file, JsonUtils.toJson(snapshot));
            long size = Files.size(file);
            return new BackupInfo(filename, filename, size, Instant.now().toString(),
                    location != null ? location : "local");
        } catch (IOException e) {
            throw new RuntimeException("创建备份失败", e);
        }
    }

    public List<BackupInfo> list(String ownerId) {
        Path dir = Paths.get(BACKUP_DIR, ownerId);
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> jsonFiles = files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();
            List<BackupInfo> result = new ArrayList<>();
            for (Path p : jsonFiles) {
                try {
                    result.add(new BackupInfo(
                            p.getFileName().toString(),
                            p.getFileName().toString(),
                            Files.size(p),
                            Files.getLastModifiedTime(p).toString(),
                            "local"));
                } catch (IOException ignored) {
                }
            }
            result.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
            return result;
        } catch (IOException e) {
            throw new RuntimeException("读取备份列表失败", e);
        }
    }

    @Transactional
    public void restore(String ownerId, String backupId) {
        Path file = Paths.get(BACKUP_DIR, ownerId, backupId);
        if (!Files.exists(file)) {
            throw new RuntimeException("备份不存在: " + backupId);
        }
        try {
            String json = Files.readString(file);
            importSnapshot(ownerId, json);
        } catch (IOException e) {
            throw new RuntimeException("恢复备份失败", e);
        }
    }

    @Transactional
    public ExportResult exportData(String ownerId, String format) {
        BackupInfo info = create(ownerId, "local");
        return new ExportResult("/api/v1/backup/download/" + info.filename());
    }

    @Transactional
    public ImportResult importData(String ownerId, String data) {
        int count = importSnapshot(ownerId, data);
        return new ImportResult(count);
    }

    private Map<String, List<Object>> buildSnapshot(String ownerId) {
        Map<String, List<Object>> snapshot = new LinkedHashMap<>();
        for (TableConfig tc : TABLES) {
            List<?> rows = entityManager.createNativeQuery(
                            "SELECT * FROM " + tc.tableName() + " WHERE owner_id = ?", tc.entityClass())
                    .setParameter(1, ownerId)
                    .getResultList();
            snapshot.put(tc.tableName(), new ArrayList<>(rows));
        }
        return snapshot;
    }

    @SuppressWarnings("unchecked")
    private int importSnapshot(String ownerId, String json) {
        Map<String, List<Map<String, Object>>> snapshot = JsonUtils.fromJson(json,
                new TypeReference<Map<String, List<Map<String, Object>>>>() {});
        if (snapshot == null) {
            throw new RuntimeException("备份数据格式错误");
        }

        int imported = 0;
        for (TableConfig tc : TABLES) {
            List<Map<String, Object>> rows = snapshot.get(tc.tableName());
            if (rows == null) continue;
            for (Map<String, Object> row : rows) {
                Object entity = objectMapper.convertValue(row, tc.entityClass());
                switch (tc.tableName()) {
                    case "note_tag" -> noteTagRepository.save((NoteTag) entity);
                    case "eta_task_category" -> taskCategoryRepository.save((TaskCategory) entity);
                    case "eta_user_profile" -> userProfileRepository.save((UserProfile) entity);
                    case "eta_task" -> taskRepository.save((Task) entity);
                    case "eta_note" -> noteRepository.save((Note) entity);
                    case "eta_error" -> errorRepository.save((Error) entity);
                    case "eta_resume" -> resumeRepository.save((Resume) entity);
                    case "eta_setting" -> settingRepository.save((Setting) entity);
                }
                imported++;
            }
        }
        return imported;
    }
}
