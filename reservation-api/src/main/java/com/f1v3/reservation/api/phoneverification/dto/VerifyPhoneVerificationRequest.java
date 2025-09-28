package com.f1v3.reservation.api.phoneverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 핸드폰 인증 코드 검증 요청 DTO 클래스
 *
 * @author Seungjo, Jeong
 */
public record VerifyPhoneVerificationRequest(
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. (예: 010-0000-0000)")
        @NotBlank(message = "핸드폰 번호를 입력해주세요.")
        String phoneNumber,

        @Size(min = 5, max = 5, message = "인증 코드는 6자리 숫자여야 합니다.")
        @NotBlank(message = "인증 코드를 입력해주세요.")
        String verificationCode
) {
}
