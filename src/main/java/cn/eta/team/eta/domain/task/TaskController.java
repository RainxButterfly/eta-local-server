// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.task;

import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.task.TaskDtos.CategoryCreateRequest;
import cn.eta.team.eta.domain.task.TaskDtos.CategoryVO;
import cn.eta.team.eta.domain.task.TaskDtos.CreateRequest;
import cn.eta.team.eta.domain.task.TaskDtos.QueryRequest;
import cn.eta.team.eta.domain.task.TaskDtos.StatusRequest;
import cn.eta.team.eta.domain.task.TaskDtos.UpdateRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务管理接口。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Result<Paged<Task>> list(@AuthenticationPrincipal EtaPrincipal p, @ModelAttribute QueryRequest q) {
        return Result.ok(taskService.list(p.userId(), q));
    }

    @PostMapping
    public Result<Task> create(@AuthenticationPrincipal EtaPrincipal p, @Valid @RequestBody CreateRequest req) {
        return Result.ok(taskService.create(p.userId(), req));
    }

    @GetMapping("/{id}")
    public Result<Task> detail(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        return Result.ok(taskService.detail(id, p.userId()));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id,
                               @RequestBody UpdateRequest req) {
        return Result.ok(taskService.update(id, p.userId(), req));
    }

    @PatchMapping("/{id}/status")
    public Result<Task> updateStatus(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id,
                                     @Valid @RequestBody StatusRequest req) {
        return Result.ok(taskService.updateStatus(id, p.userId(), req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        taskService.remove(id, p.userId());
        return Result.ok();
    }

    /* ---------------- 分类 ---------------- */

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories(@AuthenticationPrincipal EtaPrincipal p) {
        return Result.ok(taskService.listCategories(p.userId()));
    }

    @PostMapping("/categories")
    public Result<CategoryVO> createCategory(@AuthenticationPrincipal EtaPrincipal p,
                                             @Valid @RequestBody CategoryCreateRequest req) {
        return Result.ok(taskService.createCategory(p.userId(), req));
    }
}