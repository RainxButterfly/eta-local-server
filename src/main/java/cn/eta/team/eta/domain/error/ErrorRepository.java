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
    Page<Error> findBySubjectContaining(String subject, Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Page<Error> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "tags" })
    Optional<Error> findById(String id);

    @Query("SELECT e.subject, COUNT(e) FROM Error e GROUP BY e.subject ORDER BY COUNT(e) DESC")
    List<Object[]> countBySubject();

    @EntityGraph(attributePaths = { "tags" })
    List<Error> findByNextReviewAtLessThanEqual(Instant now);
}
