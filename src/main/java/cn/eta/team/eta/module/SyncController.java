// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 数据同步（TODO：对接云端存储与冲突合并）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/sync")
public class SyncController {

    @PostMapping
    public Result<Map<String, String>> sync(@RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("syncedAt", Instant.now().toString()));
    }

    @GetMapping("/devices")
    public Result<List<Map<String, Object>>> devices() {
        return Result.ok(List.of());
    }

    @DeleteMapping("/devices/{id}")
    public Result<Void> removeDevice(@PathVariable String id) {
        return Result.ok();
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.ok(Map.ofEntries(
                Map.entry("lastSyncAt", (Object) null),
                Map.entry("conflicts", 0),
                Map.entry("online", true)));
    }

    @PutMapping("/conflict")
    public Result<Map<String, String>> setConflictStrategy(@RequestBody Map<String, String> body) {
        return Result.ok(Map.of("strategy", body.getOrDefault("strategy", "manual")));
    }
}