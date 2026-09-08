// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common;

import io.github.openspacedrepetition.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FsrsConfig {

    @Bean
    public Scheduler fsrsScheduler() {
        return Scheduler.builder().build();
    }
}