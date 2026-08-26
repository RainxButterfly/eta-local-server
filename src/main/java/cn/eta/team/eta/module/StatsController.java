// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 控制台统计（TODO：按任务/笔记/错题聚合真实数据）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@RequestParam(defaultValue = "w") String range) {
        // studyHours 契约：由番茄钟插件专注计时汇总而来；未安装/未计时时为 0，前端据此降级展示。
        return Result.ok(Map.of(
                "totalTasks", 5, "doneTasks", 1, "completionRate", 20,
                "doingTasks", 2, "studyHours", 0, "totalNotes", 0,
                "pendingErrors", 0, "errorMasteryRate", 0));
    }

    @GetMapping("/activity")
    public Result<List<Map<String, Object>>> activity(@RequestParam(defaultValue = "w") String range) {
        return Result.ok(List.of(
                Map.of("label", "一", "study", 4.2, "tasks", 2.4),
                Map.of("label", "二", "study", 3.8, "tasks", 3.1)));
    }

    @GetMapping("/subject-distribution")
    public Result<List<Map<String, Object>>> subjectDistribution() {
        return Result.ok(List.of(
                Map.of("label", "数学", "value", 4, "color", "#7c6cf0"),
                Map.of("label", "英语", "value", 3, "color", "#2fa47c")));
    }

    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> heatmap(@RequestParam(defaultValue = "w") String range) {
        return Result.ok(List.of(
                Map.of("date", "2026-08-19", "value", 5),
                Map.of("date", "2026-08-20", "value", 6)));
    }
}