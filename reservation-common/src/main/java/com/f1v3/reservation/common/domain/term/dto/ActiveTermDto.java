package com.f1v3.reservation.common.domain.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;

/**
 * 현재 활성화된 약관 정보 DTO
 *
 * @author Seungjo, Jeong
 */
public record ActiveTermDto(
        TermCode termCode,
        Integer version,
        String title,
        String content,
        Boolean isRequired
) {
}
