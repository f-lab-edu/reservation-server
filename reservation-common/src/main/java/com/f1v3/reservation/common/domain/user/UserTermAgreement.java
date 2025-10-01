package com.f1v3.reservation.common.domain.user;

import com.f1v3.reservation.common.domain.term.Term;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 약관 동의 정보 엔티티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "user_term_agreements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(nullable = false, updatable = false)
    private LocalDateTime agreedAt;

    @Builder
    private UserTermAgreement(User user, Term term) {
        this.user = user;
        this.term = term;
        this.agreedAt = LocalDateTime.now();
    }
}
