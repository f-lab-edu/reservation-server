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

    private static final int MAX_ATTEMPT = 3;
    private static final int VERIFICATION_EXPIRY_MINUTES = 3;
    private static final int VERIFIED_DURATION_MINUTES = 10;

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
        this.expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_EXPIRY_MINUTES);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public boolean isAlreadyVerified() {
        return Boolean.TRUE.equals(this.isVerified);
    }

    public void incrementAttempt() {
        if (this.attemptCount >= MAX_ATTEMPT) {
            throw new IllegalStateException("인증 시도 횟수를 초과했습니다.");
        }

        this.attemptCount += 1;
    }

    public boolean checkCode(String code) {
        return this.verificationCode.equals(code);
    }

    public boolean isExpiredForVerifiedDuration() {
        return LocalDateTime.now().isAfter(verifiedAt.plusMinutes(VERIFIED_DURATION_MINUTES));
    }

    public void resend(String newCode) {
        this.verificationCode = newCode;
        this.attemptCount = 0;
        this.isVerified = false;
        this.expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_EXPIRY_MINUTES);
        this.verifiedAt = null;
    }

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }
}
