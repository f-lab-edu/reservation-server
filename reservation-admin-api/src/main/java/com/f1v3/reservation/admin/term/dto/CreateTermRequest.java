package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import com.f1v3.reservation.common.validator.EnumValid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 약관 생성 요청 DTO
 *
 * @author Seungjo, Jeong
 */
@Getter
@AllArgsConstructor
public class CreateTermRequest {

    @NotBlank
    @EnumValid(enumClass = TermCode.class)
    private final String code;

    @NotBlank
    private final String title;

    @NotBlank
    @EnumValid(enumClass = TermType.class)
    private final String type;

    @Min(1)
    private final int displayOrder;

    @NotBlank
    @EnumValid(enumClass = TermStatus.class)
    private final String status;

}
