// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * 任务分类实体。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Entity
@Table(name = "eta_task_category")
@Getter
@Setter
public class TaskCategory {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    private String ownerId;

    @Column(nullable = false)
    private String name;

    private String color;
}