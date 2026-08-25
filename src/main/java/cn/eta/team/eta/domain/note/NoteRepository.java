// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 笔记数据访问层。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public interface NoteRepository extends JpaRepository<Note, String> {

    @Query("SELECT DISTINCT n FROM Note n " +
           "LEFT JOIN n.tags t " +
           "WHERE n.ownerId = :ownerId " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.body) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:tag IS NULL OR :tag = '' OR LOWER(t.name) = LOWER(:tag)) " +
           "ORDER BY n.createdAt DESC")
    Page<Note> findByOwnerIdWithFilters(@Param("ownerId") String ownerId,
                                        @Param("keyword") String keyword,
                                        @Param("tag") String tag,
                                        Pageable pageable);

    Optional<Note> findByIdAndOwnerId(String id, String ownerId);

    /** 查询引用了指定标签的所有笔记（删除标签前解绑用） */
    List<Note> findByOwnerIdAndTagsId(String ownerId, String tagId);

    /**
     * 查询当前用户所有标签及其被引用的笔记数量（未使用的标签 count 为 0）。
     * 返回 Object[]：[0]=标签名(String), [1]=笔记数量(Long)。
     */
    @Query("SELECT t.name, " +
           "(SELECT COUNT(n) FROM Note n JOIN n.tags nt WHERE nt.id = t.id AND n.ownerId = :ownerId) " +
           "FROM NoteTag t WHERE t.ownerId = :ownerId ORDER BY t.name")
    List<Object[]> listTags(@Param("ownerId") String ownerId);
}
