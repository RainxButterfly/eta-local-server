// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 备份与恢复（TODO：接入文件系统，落盘 JSON 备份）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/backup")
public class BackupController {

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        String location = body.getOrDefault("location", "local");
        return Result.ok(Map.of(
                "id", "b-1", "filename", "eta-backup.json",
                "size", 0, "location", location));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(List.of());
    }

    @PostMapping("/restore")
    public Result<Void> restore(@RequestBody Map<String, String> body) {
        return Result.ok();
    }

    @PostMapping("/export")
    public Result<Map<String, String>> export(@RequestBody Map<String, String> body) {
        return Result.ok(Map.of("downloadUrl", "/download/backup.json"));
    }

    @PostMapping("/import")
    public Result<Map<String, Integer>> importData(@RequestBody Map<String, String> body) {
        return Result.ok(Map.of("imported", 0));
    }
}