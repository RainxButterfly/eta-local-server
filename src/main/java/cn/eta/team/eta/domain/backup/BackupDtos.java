// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.backup;

public final class BackupDtos {

    private BackupDtos() {
    }

    public record BackupInfo(
            String id,
            String filename,
            long size,
            String createdAt,
            String location) {
    }

    public record CreateRequest(String location) {
    }

    public record RestoreRequest(String backupId) {
    }

    public record ExportRequest(String format) {
    }

    public record ImportRequest(String data) {
    }

    public record ImportResult(int imported) {
    }

    public record ExportResult(String downloadUrl) {
    }
}
