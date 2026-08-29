// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "note_tag")
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class NoteTag {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column(length = 20)
    private String color;

    private Instant updatedAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "owner_id", length = 36)
    private String ownerId;
}
