package com.f1v3.reservation.api.phoneverification;

import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationRequest;
import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationResponse;
import com.f1v3.reservation.api.phoneverification.dto.VerifyPhoneVerificationRequest;
import com.f1v3.reservation.api.phoneverification.sms.SmsProvider;
import com.f1v3.reservation.api.phoneverification.strategy.NewVerificationStrategy;
import com.f1v3.reservation.api.phoneverification.strategy.PhoneVerificationStrategy;
import com.f1v3.reservation.api.phoneverification.strategy.ResendVerificationStrategy;
import com.f1v3.reservation.api.user.UserValidationService;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import com.f1v3.reservation.common.domain.phoneverification.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

import static com.f1v3.reservation.common.api.error.ErrorCode.*;

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
    private final UserValidationService userValidationService;
    private final SmsProvider smsProvider;

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public SendPhoneVerificationResponse sendVerifyCode(SendPhoneVerificationRequest request) {
        String verificationCode = generateVerificationCode();
        PhoneVerificationStrategy strategy = determineStrategy(request.phoneNumber(), verificationCode);
        PhoneVerification verification = strategy.execute(request.phoneNumber());

        phoneVerificationRepository.save(verification);
        sendSms(request.phoneNumber(), verificationCode);
        return new SendPhoneVerificationResponse(verification.getExpiredAt());
    }

    @Transactional
    public void incrementAttempt(String phoneNumber) {
        PhoneVerification verification = phoneVerificationRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ReservationException(PHONE_VERIFICATION_NOT_FOUND, log::info));

        verification.incrementAttempt();
    }

    @Transactional
    public void verifyCode(VerifyPhoneVerificationRequest request) {
        PhoneVerification verification = phoneVerificationRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new ReservationException(PHONE_VERIFICATION_NOT_FOUND, log::info));

        if (verification.isAlreadyVerified()) {
            throw new ReservationException(PHONE_VERIFICATION_ALREADY_VERIFIED, log::info);
        }

        if (verification.isExpired()) {
            throw new ReservationException(PHONE_VERIFICATION_CODE_EXPIRED, log::info);
        }

        if (verification.isExceededMaxAttempts()) {
            throw new ReservationException(PHONE_VERIFICATION_ATTEMPTS_EXCEEDED, log::info);
        }

        if (!verification.checkCode(request.verificationCode())) {
            throw new ReservationException(PHONE_VERIFICATION_CODE_INVALID, log::info);
        }

        userValidationService.checkPhoneNumberExists(request.phoneNumber());
        verification.verify();

        phoneVerificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public void checkVerified(String phoneNumber) {
        PhoneVerification verification = phoneVerificationRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ReservationException(PHONE_VERIFICATION_NOT_FOUND, log::info));

        if (!verification.isAlreadyVerified()) {
            throw new ReservationException(PHONE_VERIFICATION_NOT_VERIFIED, log::info);
        }

        if (verification.isExpiredForVerifiedDuration()) {
            throw new ReservationException(PHONE_VERIFICATION_INFO_EXPIRED, log::info);
        }
    }

    /**
     * SMS 메시지 생성
     */
    private void sendSms(String phoneNumber, String verificationCode) {
        String message = String.format("[예약 시스템] 인증번호 [%s]를 입력해주세요.", verificationCode);
        smsProvider.send(phoneNumber, message);
    }

    /**
     * 5자리 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%05d", RANDOM.nextInt(10000, 100000));
    }

    /**
     * 핸드폰 인증 전략 결정 메서드 (재전송 또는 신규 생성)
     */
    private PhoneVerificationStrategy determineStrategy(String phoneNumber, String verificationCode) {
        return phoneVerificationRepository.findByPhoneNumber(phoneNumber)
                .<PhoneVerificationStrategy>map(existing ->
                        new ResendVerificationStrategy(existing, verificationCode))
                .orElseGet(() -> new NewVerificationStrategy(verificationCode));
    }
}
