// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, String> {

    boolean existsByNameAndOwnerId(String name, String ownerId);

    List<TaskCategory> findAllByOwnerId(String ownerId);
}
