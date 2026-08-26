// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.feedback;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.security.EtaPrincipal;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

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
@RequiredArgsConstructor
public class FeedbackController {

    private final RestClient restClient;
    // 远程服务器地址
    @Value("${eta.remote-service.location}")
    private String location;

    @GetMapping("/faq")
    public Result<List<Map<String, String>>> faq() throws IOException {
        List<Map<String, String>> faqs = JsonUtils.fromJsonFile(
                "src/main/resources/faq.json",
                new TypeReference<List<Map<String, String>>>() {
                });
        return Result.ok(faqs);
    }

    @PostMapping
    public Result<Void> submit(@AuthenticationPrincipal EtaPrincipal p,
            @RequestBody Map<String, String> body) {
        restClient.post()
                .uri(location)
                .body(Map.of("id", p.userId(), "data", body))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, resp) -> {
                    throw new RuntimeException("客户端错误: " + resp.getStatusCode());
                })
                .onStatus(status -> status.is5xxServerError(), (req, resp) -> {
                    throw new RuntimeException("服务端错误: " + resp.getStatusCode());
                })
                .body(Result.class);
        return Result.ok();
    }
}