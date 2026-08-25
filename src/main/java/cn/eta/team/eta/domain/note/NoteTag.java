// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * 笔记标签实体。每个用户私有，同一用户下标签名唯一。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Entity
@Table(name = "note_tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_note_tag_owner_name", columnNames = {"owner_id", "name"})
})
@Getter
@Setter
public class NoteTag {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String ownerId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 20)
    private String color;
}
