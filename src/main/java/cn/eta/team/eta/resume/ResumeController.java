// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.resume;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.resume.ResumeDtos.CreateRequest;
import cn.eta.team.eta.resume.ResumeDtos.QueryRequest;
import cn.eta.team.eta.resume.ResumeDtos.UpdateRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

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
    public Result<Paged<Resume>> list(@AuthenticationPrincipal EtaPrincipal p, @ModelAttribute QueryRequest q) {
        return Result.ok(resumeService.list(p.userId(), q));
    }

    @PostMapping
    public Result<Resume> create(@AuthenticationPrincipal EtaPrincipal p, @Valid @RequestBody CreateRequest q ) {
        return Result.ok(resumeService.create(p.userId(), q));
    }

    @GetMapping("/{id}")
    public Result<Resume> detail(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        return Result.ok(resumeService.detail(id, p.userId()));
    }

    @PutMapping("/{id}")
    public Result<Resume> update(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id, @RequestBody UpdateRequest q) {
        return Result.ok(resumeService.update(p.userId(), id, q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        resumeService.delete(p.userId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/export")
    public Result<Map<String, String>> export(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id, @RequestBody Map<String, String> body) {
        String format = body.getOrDefault("format", "html");
        if (!List.of("pdf", "html", "markdown").contains(format)) {
            throw new BizException(ErrorCode.RESUME_EXPORT_FAILED);
        }
        resumeService.export(p.userId(), id, format);
        return Result.ok(Map.of("downloadUrl", "/download/resumes/" + id + "." + format));
    }
}