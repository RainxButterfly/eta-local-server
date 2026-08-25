// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, String> {

    @Query("SELECT DISTINCT n FROM Note n " +
           "LEFT JOIN n.tags t " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.body) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:tag IS NULL OR :tag = '' OR LOWER(t.name) = LOWER(:tag)) " +
           "ORDER BY n.createdAt DESC")
    Page<Note> findWithFilters(@Param("keyword") String keyword,
                               @Param("tag") String tag,
                               Pageable pageable);

    @Query("SELECT t.name, " +
           "(SELECT COUNT(n) FROM Note n JOIN n.tags nt WHERE nt.id = t.id) " +
           "FROM NoteTag t ORDER BY t.name")
    List<Object[]> listTags();

    List<Note> findByTagsId(String tagId);
}
