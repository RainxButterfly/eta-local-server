// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.security.EtaPrincipal;
import tools.jackson.core.type.TypeReference;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户反馈
 *
 * @author StarLeaf-Roxy ormisnal
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @GetMapping("/faq")
    public Result<List<Map<String, String>>> faq() throws IOException {
        List<Map<String, String>> faqs = JsonUtils.fromJsonFile(
                "src/main/resources/faq.json",
                new TypeReference<List<Map<String, String>>>() {
                });
        return Result.ok(faqs);
    }

    @PostMapping
    public Result<Void> submit(@AuthenticationPrincipal EtaPrincipal p, @RequestBody Map<String, String> body) {
        return Result.ok();
    }
}