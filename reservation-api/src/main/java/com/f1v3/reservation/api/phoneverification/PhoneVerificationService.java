package com.f1v3.reservation.api.phoneverification;

import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationRequest;
import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationResponse;
import com.f1v3.reservation.api.phoneverification.sms.SmsProvider;
import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import com.f1v3.reservation.common.domain.phoneverification.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 핸드폰 인증 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsProvider smsProvider;

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    @Transactional
    public SendPhoneVerificationResponse sendVerifyCode(SendPhoneVerificationRequest request) {

        String verificationCode = generateVerificationCode();

        PhoneVerification verification = PhoneVerification.builder()
                .phoneNumber(request.phoneNumber())
                .verificationCode(verificationCode)
                .build();

        String message = createSmsMessage(verificationCode);
        smsProvider.send(request.phoneNumber(), message);

        PhoneVerification savedVerification = phoneVerificationRepository.save(verification);

        return new SendPhoneVerificationResponse(savedVerification.getExpiredAt());
    }

    private String createSmsMessage(String verificationCode) {
        return String.format("[예약 시스템] 인증번호 [%s]를 입력해주세요.", verificationCode);
    }

    /**
     * 5자리 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%05d", RANDOM.nextInt(10000, 100000));
    }
}
