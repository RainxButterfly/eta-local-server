// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
