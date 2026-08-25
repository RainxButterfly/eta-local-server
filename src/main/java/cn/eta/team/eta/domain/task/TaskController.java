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
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Result<Paged<Task>> list(@ModelAttribute QueryRequest q) {
        return Result.ok(taskService.list(q));
    }

    @PostMapping
    public Result<Task> create(@Valid @RequestBody CreateRequest req) {
        return Result.ok(taskService.create(req));
    }

    @GetMapping("/{id}")
    public Result<Task> detail(@PathVariable String id) {
        return Result.ok(taskService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable String id, @Valid @RequestBody UpdateRequest req) {
        return Result.ok(taskService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    public Result<Task> updateStatus(@PathVariable String id, @Valid @RequestBody StatusRequest req) {
        return Result.ok(taskService.updateStatus(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        taskService.remove(id);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.ok(taskService.listCategories());
    }

    @PostMapping("/categories")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryCreateRequest req) {
        return Result.ok(taskService.createCategory(req));
    }
}
