// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 全局搜索（TODO：接入 MySQL 全文索引或数据库 LIKE 检索）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping
    public Result<Map<String, Object>> search(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BizException(ErrorCode.SEARCH_KEYWORD_EMPTY);
        }
        return Result.ok(Map.of(
                "keyword", keyword,
                "total", 0,
                "groups", List.of(
                        Map.of("type", "task", "name", "任务", "items", List.of()),
                        Map.of("type", "note", "name", "笔记", "items", List.of()),
                        Map.of("type", "error", "name", "错题", "items", List.of()))));
    }
}