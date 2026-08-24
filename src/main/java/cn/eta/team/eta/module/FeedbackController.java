// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户反馈（TODO：写入数据库或对接第三方工单）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @GetMapping("/faq")
    public Result<List<Map<String, String>>> faq() {
        return Result.ok(List.of(
                Map.of("question", "数据存储在哪里？", "answer", "默认存储在本机，可在「备份与恢复」中迁移看云。"),
                Map.of("question", "如何参与开源贡献？", "answer", "欢迎在 GitHub 提交 Issue 或 Pull Request。")));
    }

    @PostMapping
    public Result<Void> submit(@RequestBody Map<String, String> body) {
        return Result.ok();
    }
}