// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

/**
 * 任务实体。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Entity
@Table(name = "eta_task")
@Getter
@Setter
public class Task {

    /** 使用 UUID 字符串作为主键，与前端字符串 id 对齐 */
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    /** 归属用户 */
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String categoryId;

    /** 冗余的分组名（对应前端 Task.category） */
    private String category;

    private String tag;

    /** 主题色标识：violet/blue/amber/green/cyan/red */
    private String tone = "violet";

    /** 截止时间展示文本（如 今天 18:00） */
    private String due;

    /** 截止时间时间戳 */
    private Instant dueAt;

    /** todo/doing/done */
    private String status = "todo";

    private Integer progress = 0;

    /** 高/中/低 */
    private String priority = "中";

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}