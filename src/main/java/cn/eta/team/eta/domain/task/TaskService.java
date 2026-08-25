// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.task;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.domain.task.TaskDtos.CategoryCreateRequest;
import cn.eta.team.eta.domain.task.TaskDtos.CategoryVO;
import cn.eta.team.eta.domain.task.TaskDtos.CreateRequest;
import cn.eta.team.eta.domain.task.TaskDtos.QueryRequest;
import cn.eta.team.eta.domain.task.TaskDtos.StatusRequest;
import cn.eta.team.eta.domain.task.TaskDtos.UpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 任务业务：以当前登录用户为数据边界（ownerId 隔离）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Service
public class TaskService {

    private static final Set<String> STATUSES = Set.of("todo", "doing", "done");
    private static final Set<String> PRIORITIES = Set.of("高", "中", "低");

    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, TaskCategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    /** 分页查询当前用户的任务（支持状态 / 分类 / 关键词筛选） */
    @Transactional(readOnly = true)
    public Paged<Task> list(String ownerId, QueryRequest q) {
        int pageNum = q.page() != null ? q.page() - 1 : 0;
        int page = PageUtils.page(pageNum);
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size);

        String keyword = q.keyword() == null ? null : q.keyword().trim();
        Page<Task> taskPage = taskRepository.findByOwnerIdWithFilters(
                ownerId, q.status(), q.categoryId(), keyword, pageable);

        return Paged.of(taskPage);
    }

    @Transactional(readOnly = true)
    public Task detail(String id, String ownerId) {
        return requireOwnedTask(id, ownerId);
    }

    @Transactional
    public Task create(String ownerId, CreateRequest req) {
        if (!StringUtils.hasText(req.title())) {
            throw new BizException(ErrorCode.TASK_TITLE_EMPTY);
        }
        if (req.priority() != null && !PRIORITIES.contains(req.priority())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "优先级仅支持 高/中/低");
        }
        Task task = new Task();
        task.setOwnerId(ownerId);
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setCategoryId(req.categoryId());
        task.setCategory(req.category());
        task.setTag(req.tag());
        task.setDue(req.due());
        task.setDueAt(req.dueAt());
        task.setPriority(StringUtils.hasText(req.priority()) ? req.priority() : "中");
        task.setStatus("todo");
        task.setProgress(0);
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return taskRepository.save(task);
    }

    @Transactional
    public Task update(String id, String ownerId, UpdateRequest req) {
        Task task = requireOwnedTask(id, ownerId);
        if (req.title() != null) {
            task.setTitle(req.title());
        }
        if (req.description() != null) {
            task.setDescription(req.description());
        }
        if (req.categoryId() != null) {
            task.setCategoryId(req.categoryId());
        }
        if (req.category() != null) {
            task.setCategory(req.category());
        }
        if (req.tag() != null) {
            task.setTag(req.tag());
        }
        if (req.due() != null) {
            task.setDue(req.due());
        }
        if (req.dueAt() != null) {
            task.setDueAt(req.dueAt());
        }
        if (req.priority() != null) {
            if (!PRIORITIES.contains(req.priority())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "优先级仅支持 高/中/低");
            }
            task.setPriority(req.priority());
        }
        if (req.progress() != null) {
            task.setProgress(Math.max(0, Math.min(100, req.progress())));
        }
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateStatus(String id, String ownerId, StatusRequest req) {
        if (!STATUSES.contains(req.status())) {
            throw new BizException(ErrorCode.ILLEGAL_TASK_STATUS);
        }
        Task task = requireOwnedTask(id, ownerId);
        task.setStatus(req.status());
        if (req.progress() != null) {
            task.setProgress(Math.max(0, Math.min(100, req.progress())));
        }
        if ("done".equals(req.status())) {
            task.setProgress(100);
        }
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    @Transactional
    public void remove(String id, String ownerId) {
        Task task = requireOwnedTask(id, ownerId);
        taskRepository.delete(task);
    }

    /* ---------------- 分类管理 ---------------- */

    @Transactional(readOnly = true)
    public List<CategoryVO> listCategories(String ownerId) {
        return categoryRepository.findByOwnerId(ownerId).stream()
                .map(c -> new CategoryVO(c.getId(), c.getName(), c.getColor(),
                        taskRepository.countByOwnerIdAndCategoryId(ownerId, c.getId())))
                .toList();
    }

    @Transactional
    public CategoryVO createCategory(String ownerId, CategoryCreateRequest req) {
        if (categoryRepository.existsByOwnerIdAndName(ownerId, req.name())) {
            throw new BizException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        TaskCategory category = new TaskCategory();
        category.setOwnerId(ownerId);
        category.setName(req.name());
        category.setColor(req.color());
        categoryRepository.save(category);
        return new CategoryVO(category.getId(), category.getName(), category.getColor(), 0);
    }

    private Task requireOwnedTask(String id, String ownerId) {
        return taskRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new BizException(ErrorCode.TASK_NOT_FOUND));
    }
}