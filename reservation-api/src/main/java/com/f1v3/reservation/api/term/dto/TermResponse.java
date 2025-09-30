package com.f1v3.reservation.api.term.dto;

import com.f1v3.reservation.common.domain.term.dto.ActiveTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import lombok.AccessLevel;
import lombok.Builder;

/**
 * 약관 응답 DTO
 *
 * @author Seungjo, Jeong
 */
@Builder(access = AccessLevel.PRIVATE)
public record TermResponse(
        Long termId,
        TermCode termCode,
        Integer version,
        String title,
        String content,
        Boolean isRequired,
        Integer displayOrder
) {
    public static TermResponse from(ActiveTermDto term) {
        return TermResponse.builder()
                .termId(term.termId())
                .termCode(term.termCode())
                .version(term.version())
                .title(term.title())
                .content(term.content())
                .isRequired(term.isRequired())
                .displayOrder(term.displayOrder())
                .build();
    }
}