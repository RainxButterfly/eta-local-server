// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 任务数据访问层。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByOwnerId(String ownerId);

    Optional<Task> findByIdAndOwnerId(String id, String ownerId);

    long countByOwnerIdAndCategoryId(String ownerId, String categoryId);
}