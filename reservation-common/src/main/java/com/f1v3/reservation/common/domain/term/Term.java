package com.f1v3.reservation.common.domain.term;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
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
    private Pk pk;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isRequired;

    @Column(nullable = false)
    private LocalDateTime activatedAt;

    private LocalDateTime deactivatedAt;

    @Builder
    private Term(TermCode code, Integer version, String title, String content,
                 Integer displayOrder, Boolean isRequired,
                 LocalDateTime activatedAt, LocalDateTime deactivatedAt) {
        this.pk = new Pk(code, version);
        this.title = title;
        this.content = content;
        this.displayOrder = displayOrder;
        this.isRequired = isRequired;
        this.activatedAt = activatedAt;
        this.deactivatedAt = deactivatedAt;

        validateDisplayOrder();
    }

    /**
     * 필수 약관: 0 ~ 500번, 선택 약관: 501 ~ 1000번
     */
    private void validateDisplayOrder() {
        if (Boolean.TRUE.equals(isRequired)) {
            if (displayOrder < 0 || displayOrder > 500) {
                throw new ReservationException(ErrorCode.TERM_REQUIRED_DISPLAY_ORDER_INVALID, log::warn);
            }
        } else {
            if (displayOrder < 501 || displayOrder > 1000) {
                throw new ReservationException(ErrorCode.TERM_OPTIONAL_DISPLAY_ORDER_INVALID, log::warn);
            }
        }
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TermCode code;

        @Column(nullable = false)
        private Integer version;
    }
}
