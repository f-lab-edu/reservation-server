package com.f1v3.reservation.api.phoneverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 핸드폰 인증 요청 DTO 클래스
 *
 * @author Seungjo, Jeong
 */
public record SendPhoneVerificationRequest(
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. (예: 010-0000-0000)")
        @NotBlank(message = "핸드폰 번호를 입력해주세요.")
        String phoneNumber
) {
}
