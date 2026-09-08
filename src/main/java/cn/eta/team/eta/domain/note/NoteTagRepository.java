// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteTagRepository extends JpaRepository<NoteTag, String> {

    Optional<NoteTag> findByNameAndOwnerId(String name, String ownerId);

    boolean existsByNameAndOwnerId(String name, String ownerId);
}
