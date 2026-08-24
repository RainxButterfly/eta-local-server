// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.resume;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.common.util.JsonUtils;
import tools.jackson.core.type.TypeReference;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 简历制作（TODO：接入 JPA / 模板引擎 / 导出服务）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/resumes")
public class ResumeController {

    /**
     * 模板列表
     * 从静态资源读取
     * @throws IOException
     */
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> templates() throws IOException {
        List<Map<String, Object>> templates = JsonUtils.fromJsonFile(
                "src/main/resources/templates-config.json",
                new TypeReference<List<Map<String, Object>>>() {
            });
        return Result.ok(templates);
    }

    @GetMapping
    public Result<Paged<?>> list() {
        return Result.ok(Paged.of(List.of(), 0, 0, 20));
    }

    @PostMapping
    public Result<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        return Result.ok(Map.of("id", "r-new-1"));
    }

    @GetMapping("/{id}")
    public Result<Object> detail(@PathVariable String id) {
        if ("r-new-1".equals(id)) {
            return Result.ok(Map.of("id", id));
        }
        throw new BizException(ErrorCode.RESUME_NOT_FOUND);
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return Result.ok(body);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        return Result.ok();
    }

    @PostMapping("/{id}/export")
    public Result<Map<String, String>> export(@PathVariable String id, @RequestBody Map<String, String> body) {
        String format = body.getOrDefault("format", "html");
        if (!List.of("pdf", "html", "markdown").contains(format)) {
            throw new BizException(ErrorCode.RESUME_EXPORT_FAILED);
        }
        return Result.ok(Map.of("downloadUrl", "/download/resumes/" + id + "." + format));
    }
}