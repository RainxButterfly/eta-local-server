// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.sync;

import cn.eta.team.eta.auth.UserAccount;
import cn.eta.team.eta.auth.UserAccountRepository;
import cn.eta.team.eta.auth.UserProfile;
import cn.eta.team.eta.auth.UserProfileRepository;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.domain.error.Error;
import cn.eta.team.eta.domain.error.ErrorRepository;
import cn.eta.team.eta.domain.note.Note;
import cn.eta.team.eta.domain.note.NoteRepository;
import cn.eta.team.eta.domain.note.NoteTag;
import cn.eta.team.eta.domain.note.NoteTagRepository;
import cn.eta.team.eta.domain.resume.Resume;
import cn.eta.team.eta.domain.resume.ResumeRepository;
import cn.eta.team.eta.domain.sync.SyncDtos.DeviceInfo;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncMeta;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncRequest;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncResponse;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncStatus;
import cn.eta.team.eta.domain.sync.SyncDtos.UserAccountSnapshot;
import cn.eta.team.eta.domain.task.Task;
import cn.eta.team.eta.domain.task.TaskCategory;
import cn.eta.team.eta.domain.task.TaskCategoryRepository;
import cn.eta.team.eta.domain.task.TaskRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudSyncService {

    private static final String USER_ACCOUNT_KEY = "user-account.json";
    private static final String SNAPSHOT_KEY = "snapshot.json";
    private static final String META_KEY = "sync-meta.json";

    private record TableConfig(String tableName, Class<?> entityClass) {
    }

    private static final List<TableConfig> TABLES = List.of(
            new TableConfig("note_tag", NoteTag.class),
            new TableConfig("eta_task_category", TaskCategory.class),
            new TableConfig("eta_user_profile", UserProfile.class),
            new TableConfig("eta_task", Task.class),
            new TableConfig("eta_note", Note.class),
            new TableConfig("eta_error", Error.class),
            new TableConfig("eta_resume", Resume.class)
    );

    private static final Map<String, List<String>> MODULE_TABLES = Map.of(
            "tasks", List.of("eta_task", "eta_task_category"),
            "errors", List.of("eta_error"),
            "notes", List.of("eta_note", "note_tag"),
            "settings", List.of("eta_user_profile", "eta_resume")
    );

    private final CloudStorageProvider cloudStorage;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final ErrorRepository errorRepository;
    private final ResumeRepository resumeRepository;
    private final DeviceIdProvider deviceIdProvider;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public CloudSyncService(CloudStorageProvider cloudStorage,
                            UserAccountRepository userAccountRepository,
                            UserProfileRepository userProfileRepository,
                            NoteRepository noteRepository,
                            NoteTagRepository noteTagRepository,
                            TaskRepository taskRepository,
                            TaskCategoryRepository taskCategoryRepository,
                            ErrorRepository errorRepository,
                            ResumeRepository resumeRepository,
                            DeviceIdProvider deviceIdProvider,
                            EntityManager entityManager,
                            ObjectMapper objectMapper) {
        this.cloudStorage = cloudStorage;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.noteRepository = noteRepository;
        this.noteTagRepository = noteTagRepository;
        this.taskRepository = taskRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.errorRepository = errorRepository;
        this.resumeRepository = resumeRepository;
        this.deviceIdProvider = deviceIdProvider;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncResponse sync(String userId, SyncRequest req) {
        String now = Instant.now().toString();
        List<TableConfig> tables = resolveTables(req);

        SyncMeta remoteMeta = loadMeta(userId);
        if (remoteMeta.dataVersion() > 0) {
            downloadInternal(userId, tables);
        }

        uploadInternal(userId, now, tables);

        return new SyncResponse(now);
    }

    private List<TableConfig> resolveTables(SyncRequest req) {
        if (req == null || (req.tasks() == null && req.errors() == null
                && req.notes() == null && req.settings() == null)) {
            return TABLES;
        }
        List<String> selected = new ArrayList<>();
        if (Boolean.TRUE.equals(req.tasks())) selected.addAll(MODULE_TABLES.get("tasks"));
        if (Boolean.TRUE.equals(req.errors())) selected.addAll(MODULE_TABLES.get("errors"));
        if (Boolean.TRUE.equals(req.notes())) selected.addAll(MODULE_TABLES.get("notes"));
        if (Boolean.TRUE.equals(req.settings())) selected.addAll(MODULE_TABLES.get("settings"));
        if (selected.isEmpty()) {
            return TABLES;
        }
        return TABLES.stream().filter(t -> selected.contains(t.tableName())).toList();
    }

    public SyncStatus getStatus(String userId) {
        SyncMeta meta = loadMeta(userId);
        boolean online = true;
        try {
            cloudStorage.list(userId + "/");
        } catch (IOException e) {
            online = false;
        }
        return new SyncStatus(meta.lastSyncAt(), 0, online);
    }

    public List<DeviceInfo> listDevices(String userId) {
        SyncMeta meta = loadMeta(userId);
        String currentId = deviceIdProvider.getDeviceId();
        return meta.devices().stream()
                .map(d -> new DeviceInfo(d.id(), d.name(), d.platform(), d.id().equals(currentId), d.lastSyncAt()))
                .toList();
    }

    public void removeDevice(String userId, String deviceId) {
        SyncMeta meta = loadMeta(userId);
        List<DeviceInfo> remaining = new ArrayList<>();
        for (DeviceInfo d : meta.devices()) {
            if (!d.id().equals(deviceId)) {
                remaining.add(d);
            }
        }
        SyncMeta updated = new SyncMeta(userId, meta.lastSyncAt(), meta.lastSyncDevice(),
                meta.lastSyncPlatform(), meta.dataVersion(), remaining);
        safeUpload(userId + "/" + META_KEY, JsonUtils.toJson(updated).getBytes());
    }

    @Transactional(readOnly = true)
    protected void uploadInternal(String userId, String now, List<TableConfig> tables) {
        UserAccount account = userAccountRepository.findById(userId).orElseThrow();
        safeUpload(userId + "/" + USER_ACCOUNT_KEY, JsonUtils.toJson(toSnapshot(account)).getBytes());

        Map<String, List<Object>> snapshot = new LinkedHashMap<>();
        for (TableConfig tc : tables) {
            List<?> rows = entityManager.createNativeQuery(
                            "SELECT * FROM " + tc.tableName() + " WHERE owner_id = ?", tc.entityClass())
                    .setParameter(1, userId)
                    .getResultList();
            snapshot.put(tc.tableName(), new ArrayList<>(rows));
        }

        try {
            safeUpload(userId + "/" + SNAPSHOT_KEY, objectMapper.writeValueAsBytes(snapshot));
        } catch (Exception e) {
            throw new RuntimeException("序列化同步快照失败", e);
        }

        SyncMeta meta = loadMeta(userId);
        SyncMeta updated = updateMeta(meta, userId, now);
        safeUpload(userId + "/" + META_KEY, JsonUtils.toJson(updated).getBytes());
    }

    @Transactional
    protected void downloadInternal(String userId, List<TableConfig> tables) {
        String prefix = userId + "/";

        if (!safeExists(prefix + META_KEY)) {
            return;
        }

        byte[] accountJson = safeDownload(prefix + USER_ACCOUNT_KEY);
        UserAccountSnapshot snapshot = JsonUtils.fromJson(new String(accountJson), UserAccountSnapshot.class);

        if (!userAccountRepository.existsById(snapshot.id())) {
            UserAccount account = new UserAccount();
            account.setId(snapshot.id());
            account.setEmail(snapshot.email());
            account.setPasswordHash(snapshot.passwordHash());
            account.setDisabled(snapshot.disabled());
            userAccountRepository.save(account);
        }

        if (!safeExists(prefix + SNAPSHOT_KEY)) {
            return;
        }

        byte[] snapshotBytes = safeDownload(prefix + SNAPSHOT_KEY);
        Map<String, List<Map<String, Object>>> remoteSnapshot;
        try {
            remoteSnapshot = objectMapper.readValue(snapshotBytes,
                    new tools.jackson.core.type.TypeReference<Map<String, List<Map<String, Object>>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("反序列化同步快照失败", e);
        }

        for (TableConfig tc : tables) {
            List<Map<String, Object>> rows = remoteSnapshot.get(tc.tableName());
            if (rows == null) continue;
            mergeTable(tc, rows);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeTable(TableConfig tc, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            String id = (String) row.get("id");
            if (id == null) continue;

            Instant remoteUpdatedAt = parseInstant(row.get("updatedAt"));
            Instant localUpdatedAt = getLocalUpdatedAt(tc.tableName(), id);

            if (localUpdatedAt != null && remoteUpdatedAt != null && !remoteUpdatedAt.isAfter(localUpdatedAt)) {
                continue;
            }

            Object entity = objectMapper.convertValue(row, tc.entityClass());
            switch (tc.tableName()) {
                case "note_tag" -> noteTagRepository.save((NoteTag) entity);
                case "eta_task_category" -> taskCategoryRepository.save((TaskCategory) entity);
                case "eta_user_profile" -> userProfileRepository.save((UserProfile) entity);
                case "eta_task" -> taskRepository.save((Task) entity);
                case "eta_note" -> noteRepository.save((Note) entity);
                case "eta_error" -> errorRepository.save((Error) entity);
                case "eta_resume" -> resumeRepository.save((Resume) entity);
            }
        }
    }

    private Instant getLocalUpdatedAt(String tableName, String id) {
        List<?> result = entityManager.createNativeQuery(
                        "SELECT updated_at FROM " + tableName + " WHERE id = ?")
                .setParameter(1, id)
                .getResultList();
        if (result.isEmpty()) return null;
        Object val = result.get(0);
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toInstant();
        if (val instanceof Instant inst) return inst;
        return null;
    }

    private Instant parseInstant(Object val) {
        if (val == null) return null;
        if (val instanceof Instant inst) return inst;
        if (val instanceof String s) return Instant.parse(s);
        return null;
    }

    private SyncMeta loadMeta(String userId) {
        String key = userId + "/" + META_KEY;
        if (!safeExists(key)) {
            return SyncMeta.empty(userId);
        }
        byte[] data = safeDownload(key);
        SyncMeta meta = JsonUtils.tryFromJson(new String(data), SyncMeta.class);
        return meta != null ? meta : SyncMeta.empty(userId);
    }

    private SyncMeta updateMeta(SyncMeta meta, String userId, String now) {
        String deviceId = deviceIdProvider.getDeviceId();
        String platform = detectPlatform();
        List<DeviceInfo> devices = new ArrayList<>(meta.devices());
        devices.removeIf(d -> d.id().equals(deviceId));
        devices.add(new DeviceInfo(deviceId, "Device-" + deviceId.substring(0, 8), platform, true, now));
        return new SyncMeta(userId, now, deviceId, platform, meta.dataVersion() + 1, devices);
    }

    private String detectPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return "Windows";
        if (os.contains("mac")) return "macOS";
        if (os.contains("nux") || os.contains("nix")) return "Linux";
        return os.isEmpty() ? "Unknown" : os;
    }

    private UserAccountSnapshot toSnapshot(UserAccount account) {
        return new UserAccountSnapshot(
                account.getId(),
                account.getEmail(),
                account.getPasswordHash(),
                account.isDisabled(),
                account.getCreatedAt() != null ? account.getCreatedAt().toString() : null);
    }

    private void safeUpload(String key, byte[] data) {
        try {
            cloudStorage.upload(key, data);
        } catch (IOException e) {
            throw new RuntimeException("云端上传失败: " + key, e);
        }
    }

    private byte[] safeDownload(String key) {
        try {
            return cloudStorage.download(key);
        } catch (IOException e) {
            throw new RuntimeException("云端下载失败: " + key, e);
        }
    }

    private boolean safeExists(String key) {
        try {
            return cloudStorage.exists(key);
        } catch (IOException e) {
            return false;
        }
    }
}
