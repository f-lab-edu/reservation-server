package com.f1v3.reservation.common.domain.user;

import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.TermVersion;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_version_id", nullable = false)
    private TermVersion termVersion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime agreedAt;

    @Builder
    private UserTermAgreement(User user, Term term, TermVersion termVersion) {
        this.user = user;
        this.term = term;
        this.termVersion = termVersion;
        this.agreedAt = LocalDateTime.now();
    }
}
