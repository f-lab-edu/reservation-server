package com.f1v3.reservation.common.domain.term;

import com.f1v3.reservation.common.domain.BaseEntity;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 엔티티 클래스.
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TermCode code;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermStatus status;

    @Column(nullable = false)
    private Integer displayOrder;

    @Builder
    private Term(TermCode code, String title, TermType type, TermStatus status, Integer displayOrder) {
        this.code = code;
        this.title = title;
        this.type = type;
        this.status = status;
        this.displayOrder = displayOrder;
    }
}
