// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 笔记（TODO：接入 JPA 与富文本存储）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    @GetMapping
    public Result<Paged<?>> list(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String tag,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(Paged.of(List.of(), 0, page, pageSize));
    }

    @PostMapping
    public Result<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("id", "n-new-1"));
    }

    @GetMapping("/{id}")
    public Result<Object> detail(@PathVariable String id) {
        return Result.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return Result.ok(body);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        return Result.ok();
    }

    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> listTags() {
        return Result.ok(List.of(
                Map.of("id", "t-1", "name", "数据结构", "count", 1),
                Map.of("id", "t-2", "name", "英语", "count", 1),
                Map.of("id", "t-3", "name", "求职", "count", 1)));
    }

    @PostMapping("/tags")
    public Result<Map<String, Object>> createTag(@RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("id", "t-new", "name", String.valueOf(body.get("name")), "count", 0));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> removeTag(@PathVariable String id) {
        return Result.ok();
    }
}