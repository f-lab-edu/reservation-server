package com.f1v3.reservation.api.phoneverification.strategy;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import com.f1v3.reservation.common.domain.phoneverification.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 핸드폰 인증 신규 생성 전략
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class NewVerificationStrategy implements PhoneVerificationStrategy {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final String newCode;

    @Transactional
    @Override
    public PhoneVerification execute(String phoneNumber) {

        PhoneVerification newVerification = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(newCode)
                .build();

        return phoneVerificationRepository.save(newVerification);
    }
}
