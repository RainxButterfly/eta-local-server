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
import org.springframework.data.repository.query.Param;

public interface ErrorRepository extends JpaRepository<Error, String> {

    @EntityGraph(attributePaths = { "tags" })
    Page<Error> findBySubjectContainingAndOwnerId(String subject, String ownerId, Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Page<Error> findAllByOwnerId(String ownerId, Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Optional<Error> findById(String id);

    @Query("SELECT e.subject, COUNT(e) FROM Error e WHERE e.ownerId = :ownerId GROUP BY e.subject ORDER BY COUNT(e) DESC")
    List<Object[]> countBySubject(@Param("ownerId") String ownerId);

    @EntityGraph(attributePaths = { "tags" })
    List<Error> findByNextReviewAtLessThanEqualAndOwnerId(Instant now, String ownerId);

    @Query("SELECT e FROM Error e WHERE e.ownerId = :ownerId AND " +
           "(LOWER(e.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(e.answer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(e.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY e.createdAt DESC")
    Page<Error> findWithFilters(@Param("ownerId") String ownerId,
                       @Param("keyword") String keyword,
                       Pageable pageable);
}
