// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.error;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ErrorRepository extends JpaRepository<Error, String> {

    @EntityGraph(attributePaths = { "tags" })
    Page<Error> findByOwnerIdAndSubjectContaining(String ownerId, String subject, Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Page<Error> findByOwnerId(String ownerId, Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Optional<Error> findByIdAndOwnerId(String id, String ownerId);

    // 统计各学科错题数量（按用户筛选）
    @Query("SELECT e.subject, COUNT(e) FROM Error e WHERE e.ownerId = :ownerId GROUP BY e.subject ORDER BY COUNT(e) DESC")
    List<Object[]> countBySubject(String ownerId);

    @EntityGraph(attributePaths = { "tags" })
    List<Error> findByOwnerIdAndNextReviewAtLessThanEqual(String ownerId, Instant now);

}
