package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 관리자용 약관 응답 DTO (Term + TermVersion 정보 포함)
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
        Integer displayOrder,
        Boolean isRequired,
        LocalDateTime activatedAt,
        LocalDateTime deactivatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public static TermResponse from(AdminTermDto termDto) {
        return TermResponse.builder()
                .termId(termDto.termId())
                .termCode(termDto.termCode())
                .version(termDto.version())
                .title(termDto.title())
                .content(termDto.content())
                .displayOrder(termDto.displayOrder())
                .isRequired(termDto.isRequired())
                .activatedAt(termDto.activatedAt())
                .deactivatedAt(termDto.deactivatedAt())
                .createdAt(termDto.createdAt())
                .updatedAt(termDto.updatedAt())
                .build();
    }
}
