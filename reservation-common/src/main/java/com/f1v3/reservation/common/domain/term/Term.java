package com.f1v3.reservation.common.domain.term;

import com.f1v3.reservation.common.domain.BaseEntity;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 약관 엔티티 클래스.
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @EmbeddedId
    private TermPk termPk;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 21844)
    private String content;

    @Column(nullable = false)
    private Boolean isRequired;

    @Column(nullable = false)
    private LocalDateTime activatedAt;

    @Column(nullable = true)
    private LocalDateTime deactivatedAt;

    @Builder
    private Term(TermPk termPk, String title, String content, Boolean isRequired,
                 LocalDateTime activatedAt, LocalDateTime deactivatedAt) {
        this.termPk = termPk;
        this.title = title;
        this.content = content;
        this.isRequired = isRequired;
        this.activatedAt = activatedAt;
        this.deactivatedAt = deactivatedAt;
    }

    public void changeDeactivatedAt(LocalDateTime deactivatedAt) {
        if (this.deactivatedAt == null) {
            this.deactivatedAt = deactivatedAt;
        }
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class TermPk implements Serializable {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TermCode code;

        @Column(nullable = false)
        private Integer version;
    }
}
