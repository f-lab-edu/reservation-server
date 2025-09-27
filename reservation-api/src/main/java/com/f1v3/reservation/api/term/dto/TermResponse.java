package com.f1v3.reservation.api.term.dto;

import com.f1v3.reservation.common.domain.term.dto.CurrentTermDto;
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
        String termCode,
        Integer version,
        String title,
        String type,
        String content,
        Integer displayOrder
) {
    public static TermResponse from(CurrentTermDto term) {
        return TermResponse.builder()
                .termId(term.getTermId())
                .termCode(term.getTermCode())
                .version(term.getVersion())
                .title(term.getTitle())
                .type(term.getType())
                .content(term.getContent())
                .displayOrder(term.getDisplayOrder())
                .build();
    }
}