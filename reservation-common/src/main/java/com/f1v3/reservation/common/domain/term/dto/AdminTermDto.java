package com.f1v3.reservation.common.domain.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;

import java.time.LocalDateTime;

/**
 * 관리자용 약관 DTO (Term + TermVersion 정보 포함)
 *
 * @author Seungjo, Jeong
 */
public record AdminTermDto(
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
        LocalDateTime updatedAt) {
}
