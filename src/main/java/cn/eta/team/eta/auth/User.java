// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 用户实体。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Entity
@Table(name = "eta_user")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(length = 512)
    private String avatar;

    @Column(length = 512)
    private String bio;

    private boolean disabled = false;

    private Instant createdAt = Instant.now();
}