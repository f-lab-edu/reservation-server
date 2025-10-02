package com.f1v3.reservation.api.phoneverification.strategy;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import lombok.RequiredArgsConstructor;

/**
 * 핸드폰 인증 신규 생성 전략
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class NewVerificationStrategy implements PhoneVerificationStrategy {

    private final String newCode;

    @Override
    public PhoneVerification execute(String phoneNumber) {
        return PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(newCode)
                .build();
    }
}
