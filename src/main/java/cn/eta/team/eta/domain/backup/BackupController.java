// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.backup;

import java.util.List;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.backup.BackupDtos.BackupInfo;
import cn.eta.team.eta.domain.backup.BackupDtos.CreateRequest;
import cn.eta.team.eta.domain.backup.BackupDtos.ExportRequest;
import cn.eta.team.eta.domain.backup.BackupDtos.ExportResult;
import cn.eta.team.eta.domain.backup.BackupDtos.ImportRequest;
import cn.eta.team.eta.domain.backup.BackupDtos.ImportResult;
import cn.eta.team.eta.domain.backup.BackupDtos.RestoreRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @PostMapping
    public Result<BackupInfo> create(@AuthenticationPrincipal EtaPrincipal principal,
                                      @RequestBody(required = false) CreateRequest req) {
        String location = req != null ? req.location() : null;
        return Result.ok(backupService.create(principal.userId(), location));
    }

    @GetMapping("/list")
    public Result<List<BackupInfo>> list(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(backupService.list(principal.userId()));
    }

    @PostMapping("/restore")
    public Result<Void> restore(@AuthenticationPrincipal EtaPrincipal principal,
                                 @RequestBody RestoreRequest req) {
        backupService.restore(principal.userId(), req.backupId());
        return Result.ok();
    }

    @PostMapping("/export")
    public Result<ExportResult> export(@AuthenticationPrincipal EtaPrincipal principal,
                                        @RequestBody(required = false) ExportRequest req) {
        String format = req != null ? req.format() : "json";
        return Result.ok(backupService.exportData(principal.userId(), format));
    }

    @PostMapping("/import")
    public Result<ImportResult> importData(@AuthenticationPrincipal EtaPrincipal principal,
                                            @RequestBody ImportRequest req) {
        return Result.ok(backupService.importData(principal.userId(), req.data()));
    }
}
