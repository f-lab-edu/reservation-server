package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.api.validator.EnumValid;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 약관 생성 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record CreateTermRequest(
        @EnumValid(enumClass = TermCode.class)
        @NotBlank(message = "약관 코드를 입력해주세요.")
        String code,

        @NotBlank(message = "약관 제목을 입력해주세요.")
        @Length(max = 100, message = "약관 제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "약관 내용을 입력해주세요.")
        @Length(max = 21844, message = "약관 내용은 최대 21,844자까지 입력 가능합니다.")
        String content,

        @NotNull(message = "약관 필수 여부를 입력해주세요.")
        Boolean isRequired,

        @NotNull(message = "약관 활성화 시간을 입력해주세요.")
        LocalDateTime activatedAt,

        LocalDateTime deactivatedAt // nullable
) {
}
