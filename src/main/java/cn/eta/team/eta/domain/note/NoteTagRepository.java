// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 笔记标签数据访问层。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public interface NoteTagRepository extends JpaRepository<NoteTag, String> {

    List<NoteTag> findByOwnerId(String ownerId);

    Optional<NoteTag> findByOwnerIdAndName(String ownerId, String name);

    boolean existsByOwnerIdAndName(String ownerId, String name);

    void deleteByIdAndOwnerId(String id, String ownerId);
}
