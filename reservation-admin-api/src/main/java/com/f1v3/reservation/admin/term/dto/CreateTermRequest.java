package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.validator.EnumValid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
        String title,

        @NotBlank(message = "약관 내용을 입력해주세요.")
        String content,

        @Min(value = 1, message = "표시 순서는 1 이상의 값으로 설정해주세요. (ASCENDING)")
        int displayOrder,

        @NotNull(message = "약관 필수 여부를 입력해주세요.")
        Boolean isRequired,

        @NotNull(message = "약관 활성화 시간을 입력해주세요.")
        LocalDateTime activatedAt,

        LocalDateTime deactivatedAt // nullable
) {
}
