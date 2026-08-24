package cn.eta.team.eta.resume;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 简历实体。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
@Entity
@Table(name = "eta_resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    /** 归属用户 */
    private String ownerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String templateId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Embedded
    private ResumeContent content;
}