// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 任务分类数据访问层。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, String> {

    List<TaskCategory> findByOwnerId(Long ownerId);

    Optional<TaskCategory> findByIdAndOwnerId(String id, Long ownerId);

    boolean existsByOwnerIdAndName(Long ownerId, String name);
}