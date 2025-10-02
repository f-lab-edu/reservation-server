package com.f1v3.reservation.api.phoneverification.strategy;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import lombok.RequiredArgsConstructor;

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

    @Override
    public PhoneVerification execute(String phoneNumber) {

        boolean isResendAllowed = existingVerification.getLastSentAt().plusMinutes(RESEND_LIMIT_MINUTES)
                .isBefore(LocalDateTime.now());

        if (!isResendAllowed) {
            throw new ReservationException(ErrorCode.PHONE_VERIFICATION_RESEND_NOT_ALLOWED);
        }

        existingVerification.resend(newCode);
        return existingVerification;
    }
}
