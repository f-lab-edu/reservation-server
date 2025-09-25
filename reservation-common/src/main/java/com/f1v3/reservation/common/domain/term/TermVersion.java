package com.f1v3.reservation.common.domain.term;

import com.f1v3.reservation.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 약관 버전 엔티티 클래스.
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "term_versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isCurrent;

    @Column(nullable = true)
    private LocalDateTime effectiveDateTime;

    @Column(nullable = true)
    private LocalDateTime expiryDateTime;

    @Builder
    private TermVersion(Term term, Integer version, String content, Boolean isCurrent, LocalDateTime effectiveDateTime, LocalDateTime expiryDateTime) {
        this.term = term;
        this.version = version;
        this.content = content;
        this.isCurrent = isCurrent;
        this.effectiveDateTime = effectiveDateTime;
        this.expiryDateTime = expiryDateTime;
    }
}
