package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import com.f1v3.reservation.common.validator.EnumValid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

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

        @EnumValid(enumClass = TermType.class)
        @NotBlank(message = "약관 유형(REQUIRED/OPTIONAL)을 입력해주세요.")
        String type,

        @Min(value = 1, message = "표시 순서는 1 이상의 값으로 설정해주세요. (ASCENDING)")
        int displayOrder,

        @EnumValid(enumClass = TermStatus.class)
        @NotBlank(message = "약관의 상태(ACTIVE/INACTIVE)를 입력해주세요.")
        String status
) {
}
