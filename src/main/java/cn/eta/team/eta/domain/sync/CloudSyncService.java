// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.sync;

import cn.eta.team.eta.auth.UserAccount;
import cn.eta.team.eta.auth.UserAccountRepository;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.domain.sync.SyncDtos.DeviceInfo;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncMeta;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncResponse;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncStatus;
import cn.eta.team.eta.domain.sync.SyncDtos.UserAccountSnapshot;
import cn.eta.team.eta.tenant.TenantContext;
import cn.eta.team.eta.tenant.UserDatabaseInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CloudSyncService {

    private static final String USER_ACCOUNT_KEY = "user-account.json";
    private static final String USER_DB_BACKUP_KEY = "user-db.backup.zip";
    private static final String META_KEY = "sync-meta.json";

    private final CloudStorageProvider cloudStorage;
    private final UserAccountRepository userAccountRepository;
    private final UserDatabaseInitializer userDatabaseInitializer;
    private final DeviceIdProvider deviceIdProvider;

    @Value("${eta.user-db.path:./data/users}")
    private String userDbPath;

    public CloudSyncService(CloudStorageProvider cloudStorage,
                            UserAccountRepository userAccountRepository,
                            UserDatabaseInitializer userDatabaseInitializer,
                            DeviceIdProvider deviceIdProvider) {
        this.cloudStorage = cloudStorage;
        this.userAccountRepository = userAccountRepository;
        this.userDatabaseInitializer = userDatabaseInitializer;
        this.deviceIdProvider = deviceIdProvider;
    }

    public SyncResponse sync(String userId) {
        String now = Instant.now().toString();
        String platform = detectPlatform();

        SyncMeta remoteMeta = loadMeta(userId);
        if (remoteMeta.dataVersion() > 0) {
            downloadInternal(userId);
        }

        uploadInternal(userId, now, platform);

        return new SyncResponse(now);
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

    private void uploadInternal(String userId, String now, String platform) {
        UserAccount account = TenantContext.runAsDefault(() ->
                userAccountRepository.findById(userId).orElseThrow());

        String prefix = userId + "/";

        byte[] accountJson = JsonUtils.toJson(toSnapshot(account)).getBytes();
        safeUpload(prefix + USER_ACCOUNT_KEY, accountJson);

        byte[] backupBytes = backupUserDatabase(userId);
        if (backupBytes != null) {
            safeUpload(prefix + USER_DB_BACKUP_KEY, backupBytes);
        }

        SyncMeta meta = loadMeta(userId);
        SyncMeta updated = updateMeta(meta, userId, now, platform);
        safeUpload(prefix + META_KEY, JsonUtils.toJson(updated).getBytes());
    }

    private void downloadInternal(String userId) {
        String prefix = userId + "/";

        if (!safeExists(prefix + META_KEY)) {
            return;
        }

        byte[] accountJson = safeDownload(prefix + USER_ACCOUNT_KEY);
        UserAccountSnapshot snapshot = JsonUtils.fromJson(new String(accountJson), UserAccountSnapshot.class);

        TenantContext.runAsDefault(() -> {
            if (!userAccountRepository.existsById(snapshot.id())) {
                UserAccount account = new UserAccount();
                account.setId(snapshot.id());
                account.setEmail(snapshot.email());
                account.setPasswordHash(snapshot.passwordHash());
                account.setDisabled(snapshot.disabled());
                userAccountRepository.save(account);
            }
            return null;
        });

        if (safeExists(prefix + USER_DB_BACKUP_KEY)) {
            byte[] backupBytes = safeDownload(prefix + USER_DB_BACKUP_KEY);
            restoreUserDatabase(userId, backupBytes);
        } else {
            userDatabaseInitializer.ensureRegistered(userId);
        }
    }

    private byte[] backupUserDatabase(String userId) {
        DataSource ds = userDatabaseInitializer.getDataSource(userId);
        if (ds == null) {
            return null;
        }
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("eta-backup-", ".zip");
            try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("BACKUP TO '" + tempFile.toAbsolutePath().toString().replace("\\", "/") + "'");
            }
            return Files.readAllBytes(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("备份用户数据库失败: " + userId, e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void restoreUserDatabase(String userId, byte[] backupBytes) {
        Path tempFile = null;
        try {
            userDatabaseInitializer.closeAndRemoveDataSource(userId);

            tempFile = Files.createTempFile("eta-restore-", ".zip");
            Files.write(tempFile, backupBytes);

            Path userDir = Paths.get(userDbPath).toAbsolutePath();
            Files.createDirectories(userDir);

            String url = "jdbc:h2:file:" + userDir + "/" + userId
                    + ";MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            String backupPath = tempFile.toAbsolutePath().toString().replace("\\", "/");

            try (Connection conn = DriverManager.getConnection(url, "sa", "");
                 Statement stmt = conn.createStatement()) {
                stmt.execute("RESTORE FROM '" + backupPath + "'");
            }

            userDatabaseInitializer.ensureRegistered(userId);
        } catch (Exception e) {
            throw new RuntimeException("恢复用户数据库失败: " + userId, e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
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

    private SyncMeta updateMeta(SyncMeta meta, String userId, String now, String platform) {
        String deviceId = deviceIdProvider.getDeviceId();
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
