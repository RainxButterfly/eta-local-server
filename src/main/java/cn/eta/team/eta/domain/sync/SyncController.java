// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.sync;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.sync.SyncDtos.DeviceInfo;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncRequest;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncResponse;
import cn.eta.team.eta.domain.sync.SyncDtos.SyncStatus;
import cn.eta.team.eta.security.EtaPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final CloudSyncService cloudSyncService;

    public SyncController(CloudSyncService cloudSyncService) {
        this.cloudSyncService = cloudSyncService;
    }

    @PostMapping
    public Result<SyncResponse> sync(@AuthenticationPrincipal EtaPrincipal principal,
                                      @RequestBody(required = false) SyncRequest req) {
        return Result.ok(cloudSyncService.sync(principal.userId(), req));
    }

    @GetMapping("/devices")
    public Result<List<DeviceInfo>> devices(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(cloudSyncService.listDevices(principal.userId()));
    }

    @DeleteMapping("/devices/{id}")
    public Result<Void> removeDevice(@AuthenticationPrincipal EtaPrincipal principal,
                                      @PathVariable String id) {
        cloudSyncService.removeDevice(principal.userId(), id);
        return Result.ok();
    }

    @GetMapping("/status")
    public Result<SyncStatus> status(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(cloudSyncService.getStatus(principal.userId()));
    }

    // TODO
    @PutMapping("/conflict")
    public Result<Map<String, String>> setConflictStrategy(@RequestBody Map<String, String> body) {
        return Result.ok(Map.of("strategy", body.getOrDefault("strategy", "manual")));
    }
}
