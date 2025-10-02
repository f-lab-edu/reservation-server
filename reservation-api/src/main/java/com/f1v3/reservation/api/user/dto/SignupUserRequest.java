package com.f1v3.reservation.api.user.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.user.enums.Gender;
import com.f1v3.reservation.common.validator.EnumValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Set;

/**
 * 회원가입 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record SignupUserRequest(
        @NotBlank(message = "패스워드를 입력해주세요.")
        String password,

        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname,

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. (예: 010-0000-0000)")
        @NotBlank(message = "핸드폰 번호를 입력해주세요.")
        String phoneNumber,

        @NotNull(message = "생년월일을 입력해주세요.")
        LocalDate birth,

        @NotBlank(message = "성별을 입력해주세요.")
        @EnumValid(message = "성별은 M | F로 입력해주세요.", enumClass = Gender.class)
        String gender,

        @NotEmpty(message = "약관 동의를 해주세요.")
        Set<SignupTermRequest> agreeTerms
) {

    public record SignupTermRequest(
            @NotBlank(message = "약관 코드를 입력해주세요.")
            @EnumValid(message = "올바른 약관 코드를 입력해주세요.", enumClass = TermCode.class)
            String termCode,

            @NotNull(message = "약관 버전을 입력해주세요.")
            Integer version
    ) {
    }
}
