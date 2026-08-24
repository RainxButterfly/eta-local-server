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
 * 错题本（TODO：接入 JPA 与遗忘曲线复习调度）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/errors")
public class ErrorbookController {

    @GetMapping
    public Result<Paged<?>> list(@RequestParam(required = false) String subject,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(Paged.of(List.of(), 0, page, pageSize));
    }

    @PostMapping
    public Result<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("id", "e-new-1"));
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

    /** 学科分布统计 */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.ok(List.of(
                Map.of("subject", "数学", "count", 3),
                Map.of("subject", "英语", "count", 2),
                Map.of("subject", "计算机", "count", 1)));
    }

    /** 今日应复习错题 */
    @GetMapping("/review")
    public Result<List<Object>> review() {
        return Result.ok(List.of());
    }

    /** 提交复习结果 */
    @PostMapping("/{id}/review")
    public Result<Map<String, Object>> submitReview(@PathVariable String id,
                                                    @RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("id", id, "remembered", body.getOrDefault("remembered", false)));
    }
}