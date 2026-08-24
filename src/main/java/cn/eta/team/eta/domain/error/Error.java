package cn.eta.team.eta.domain.error;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "eta_error")
@Getter
@Setter
public class Error {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(length =  36, nullable = false)
    private String ownerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;
    @Column(length = 64)
    private String subject;

    @Column(length = 128)
    private String source;

    // TODO 这是啥字段
    @Column(length = 20)
    private String level;

    //TODO 这是啥字段
    @Column(length = 20)
    private String tone;

    private long wrongCount = 0;

    private boolean mastered = false;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant lastWrongAt;

    private Instant nextReviewAt;

    @ElementCollection
    @CollectionTable(name = "error_tags", joinColumns = @JoinColumn(name = "error_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
}