package com.f1v3.reservation.api.phoneverification.strategy;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 핸드폰 인증 재전송 전략
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class ResendVerificationStrategy implements PhoneVerificationStrategy {

    private final PhoneVerification existingVerification;
    private final String newCode;

    private static final int RESEND_LIMIT_MINUTES = 3;

    @Transactional
    @Override
    public PhoneVerification execute(String phoneNumber) {

        boolean isResendAllowed = existingVerification.getCreatedAt().plusMinutes(RESEND_LIMIT_MINUTES)
                .isBefore(LocalDateTime.now());

        if (!isResendAllowed) {
            throw new IllegalArgumentException("3분 이내에 생성한 인증 요청이 존재합니다.");
        }

        existingVerification.resend(newCode);
        return existingVerification;
    }
}
