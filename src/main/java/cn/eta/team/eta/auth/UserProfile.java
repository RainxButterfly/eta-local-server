// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "eta_user_profile")
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class UserProfile {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(length = 512)
    private String avatar;

    @Column(length = 512)
    private String bio;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "owner_id", length = 36)
    private String ownerId;
}
