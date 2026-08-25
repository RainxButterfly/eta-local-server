// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.sync;

import java.util.List;

public final class SyncDtos {

    private SyncDtos() {
    }

    public record SyncResponse(String syncedAt) {
    }

    public record SyncStatus(
            String lastSyncAt,
            int conflicts,
            boolean online) {
    }

    public record DeviceInfo(
            String id,
            String name,
            String platform,
            boolean online,
            String lastSyncAt) {
    }

    public record SyncMeta(
            String userId,
            String lastSyncAt,
            String lastSyncDevice,
            String lastSyncPlatform,
            long dataVersion,
            List<DeviceInfo> devices) {

        public static SyncMeta empty(String userId) {
            return new SyncMeta(userId, null, null, null, 0, List.of());
        }
    }

    public record UserAccountSnapshot(
            String id,
            String email,
            String passwordHash,
            boolean disabled,
            String createdAt) {
    }
}
