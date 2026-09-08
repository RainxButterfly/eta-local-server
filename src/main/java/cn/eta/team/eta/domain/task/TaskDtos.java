// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 任务模块 DTO。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class TaskDtos {

        private TaskDtos() {
        }

        /** 任务创建入参，与前端 TaskCreate 对齐 */
        public record CreateRequest(
                        @NotBlank(message = "任务标题不能为空") @Size(max = 200, message = "标题过长") String title,
                        @Size(max = 1000, message = "描述过长") String description,
                        String categoryId,
                        String category,
                        String tag,
                        String due,
                        Instant dueAt,
                        String priority) {
        }

        /** 任务更新入参 */
        public record UpdateRequest(
                        @Size(max = 200, message = "标题过长") String title,
                        @Size(max = 1000, message = "描述过长") String description,
                        String categoryId,
                        String category,
                        String tag,
                        String due,
                        Instant dueAt,
                        String priority,
                        Integer progress) {
        }

        /** 状态流转入参 */
        public record StatusRequest(@NotNull(message = "状态不能为空") String status, Integer progress) {
        }

        /** 分类创建入参 */
        public record CategoryCreateRequest(
                        @NotBlank(message = "分类名不能为空") @Size(max = 32, message = "分类名过长") String name,
                        String color) {
        }

        /** 任务查询入参 */
        public record QueryRequest(String status, String categoryId, String keyword,
                        @NotNull(message = "不能为空") Integer page, @NotNull(message = "不能为空") Integer pageSize) {
        }

        /** 分类出参（含任务计数） */
        public record CategoryVO(String id, String name, String color, long count) {
        }
}