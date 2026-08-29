// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.error;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eta_error")
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class Error {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;
    @Column(length = 64)
    private String subject;

    @Column(length = 128)
    private String source;

    @Column(length = 20)
    private String level;

    @Column(length = 20)
    private String tone;

    private Integer wrongCount = 0;

    private boolean mastered = false;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant lastWrongAt;

    private Instant nextReviewAt;

    @ElementCollection
    @CollectionTable(name = "error_tags", joinColumns = @JoinColumn(name = "error_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    //FSRS所需字段
    private double difficulty = 0.0;

    private double stability = 0.0;

    private Instant lastReviewAt;

    private Instant updatedAt = Instant.now();

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "owner_id", length = 36)
    private String ownerId;
}