// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.setting;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.setting.SettingDtos.SettingResponse;
import cn.eta.team.eta.domain.setting.SettingDtos.UpdateRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public Result<SettingResponse> get(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(settingService.get(principal.userId()));
    }

    @PutMapping
    public Result<SettingResponse> update(@AuthenticationPrincipal EtaPrincipal principal,
                                           @Valid @RequestBody UpdateRequest req) {
        return Result.ok(settingService.update(principal.userId(), req));
    }
}
