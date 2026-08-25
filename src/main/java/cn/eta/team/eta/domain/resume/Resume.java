// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.resume;

import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 简历实体。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
@Entity
@Table(name = "eta_resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String templateId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "real_name")),
        @AttributeOverride(name = "role", column = @Column(name = "role")),
        @AttributeOverride(name = "email", column = @Column(name = "email")),
        @AttributeOverride(name = "phone", column = @Column(name = "phone"))
    })
    @Embedded
    private ResumeContent content;
}