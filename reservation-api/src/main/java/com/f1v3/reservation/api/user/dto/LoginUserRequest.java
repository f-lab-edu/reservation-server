package com.f1v3.reservation.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * 로그인 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record LoginUserRequest(

        @NotEmpty(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotEmpty(message = "비밀번호를 입력해주세요.")
        String password
) {
}
