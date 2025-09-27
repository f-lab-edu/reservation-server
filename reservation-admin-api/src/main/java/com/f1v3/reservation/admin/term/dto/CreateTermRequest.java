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
        @NotBlank
        String code,

        @NotBlank
        String title,

        @EnumValid(enumClass = TermType.class)
        @NotBlank String type,

        @Min(1)
        int displayOrder,

        @EnumValid(enumClass = TermStatus.class)
        @NotBlank
        String status
) {
}
