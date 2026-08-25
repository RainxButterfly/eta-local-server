// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "eta_user_profile")
@Getter
@Setter
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
}
