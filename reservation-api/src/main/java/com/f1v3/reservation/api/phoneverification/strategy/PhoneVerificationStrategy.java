package com.f1v3.reservation.api.phoneverification.strategy;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;

/**
 * 핸드폰 인증 전략 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface PhoneVerificationStrategy {

    PhoneVerification execute(String phoneNumber);
}
