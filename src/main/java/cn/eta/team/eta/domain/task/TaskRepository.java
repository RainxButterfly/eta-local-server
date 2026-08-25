// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT t FROM Task t WHERE t.ownerId = :ownerId " +
           "AND (:status IS NULL OR :status = '' OR :status = 'all' OR t.status = :status) " +
           "AND (:categoryId IS NULL OR :categoryId = '' OR t.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<Task> findByOwnerIdWithFilters(@Param("ownerId") String ownerId,
                                        @Param("status") String status,
                                        @Param("categoryId") String categoryId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
}