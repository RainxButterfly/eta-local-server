// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.stats;

import java.util.List;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.stats.StatsDtos.ActivityPoint;
import cn.eta.team.eta.domain.stats.StatsDtos.HeatmapPoint;
import cn.eta.team.eta.domain.stats.StatsDtos.OverviewResponse;
import cn.eta.team.eta.domain.stats.StatsDtos.SubjectDistribution;
import cn.eta.team.eta.security.EtaPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<OverviewResponse> overview(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(statsService.overview(principal.userId()));
    }

    @GetMapping("/activity")
    public Result<List<ActivityPoint>> activity(@AuthenticationPrincipal EtaPrincipal principal,
                                                 @RequestParam(defaultValue = "w") String range) {
        return Result.ok(statsService.activity(principal.userId(), range));
    }

    @GetMapping("/subject-distribution")
    public Result<List<SubjectDistribution>> subjectDistribution(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(statsService.subjectDistribution(principal.userId()));
    }

    @GetMapping("/heatmap")
    public Result<List<HeatmapPoint>> heatmap(@AuthenticationPrincipal EtaPrincipal principal,
                                               @RequestParam(defaultValue = "w") String range) {
        return Result.ok(statsService.heatmap(principal.userId(), range));
    }
}
