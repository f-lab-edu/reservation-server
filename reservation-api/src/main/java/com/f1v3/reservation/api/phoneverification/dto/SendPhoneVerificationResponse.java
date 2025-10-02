package com.f1v3.reservation.api.phoneverification.dto;

import java.time.LocalDateTime;

/**
 * 핸드폰 인증 응답 DTO 클래스
 *
 * @author Seungjo, Jeong
 */
public record SendPhoneVerificationResponse(
        LocalDateTime expiredAt
) {
}
