package com.f1v3.reservation.common.domain.phoneverification;

import com.f1v3.reservation.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 핸드폰 인증 정보를 관리하는 엔티티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "phone_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String verificationCode;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = true)
    private LocalDateTime verifiedAt;

    @Builder
    public PhoneVerification(String phoneNumber, String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.attemptCount = 0;
        this.isVerified = false;
        this.expiredAt = LocalDateTime.now().plusMinutes(3); // fixme: 생성 시점에서 3분??
    }

    public void verify(String code) {
        incrementAttempt();
        validateCode(code);
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    private void incrementAttempt() {
        if (this.attemptCount >= 3) {
            throw new IllegalStateException("최대 시도 횟수를 초과했습니다.");
        }
        this.attemptCount += 1;
    }

    private void validateCode(String code) {
        if (!this.verificationCode.equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
    }

    // 쿼리 호출 시점에서 필터링하는 방식으로 접근
    private boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }
}
