// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * 笔记模块 DTO。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
public final class NoteDtos {

        private NoteDtos() {
        }

        public record CreateRequest(
                        @NotBlank(message = "标题不能为空") String title,
                        String body,
                        List<String> tagNames) {
        }

        public record UpdateRequest(
                        String title,
                        String body,
                        List<String> tagNames) {
        }

        public record QueryRequest(
                        String keyword,
                        String tag,
                        Integer page,
                        Integer pageSize) {
        }

        public record CreateTagRequest(
                        @NotBlank(message = "标签名字不能为空") String name,
                        String color) {
        }

        public record TagStat(String tag, Integer count) {
        }
}
