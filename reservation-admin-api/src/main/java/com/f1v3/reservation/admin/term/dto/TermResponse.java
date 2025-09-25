package com.f1v3.reservation.admin.term.dto;

import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 약관 응답 DTO (Term + TermVersion 정보 포함)
 *
 * @author Seungjo, Jeong
 */
@Getter
public class TermResponse {

    // Term 정보
    private final Long termId;
    private final String termCode;
    private final String title;
    private final String type;
    private final Integer displayOrder;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // TermVersion 정보
    private final Long termVersionId;
    private final Integer version;
    private final Boolean isCurrent;
    private final String content;
    private final LocalDateTime effectiveDateTime;
    private final LocalDateTime expiryDateTime;
    private final LocalDateTime termVersionCreatedAt;
    private final LocalDateTime termVersionUpdatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private TermResponse(Long termId, String termCode, String title, String type, Integer displayOrder, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                         Long termVersionId, Integer version, Boolean isCurrent, String content, LocalDateTime effectiveDateTime, LocalDateTime expiryDateTime, LocalDateTime termVersionCreatedAt, LocalDateTime termVersionUpdatedAt) {
        this.termId = termId;
        this.termCode = termCode;
        this.title = title;
        this.type = type;
        this.displayOrder = displayOrder;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.termVersionId = termVersionId;
        this.version = version;
        this.isCurrent = isCurrent;
        this.content = content;
        this.effectiveDateTime = effectiveDateTime;
        this.expiryDateTime = expiryDateTime;
        this.termVersionCreatedAt = termVersionCreatedAt;
        this.termVersionUpdatedAt = termVersionUpdatedAt;
    }

    public static TermResponse from(AdminTermDto adminTermDto) {
        return TermResponse.builder()
                .termId(adminTermDto.getTermId())
                .termCode(adminTermDto.getTermCode())
                .title(adminTermDto.getTitle())
                .type(adminTermDto.getType())
                .displayOrder(adminTermDto.getDisplayOrder())
                .status(adminTermDto.getStatus())
                .createdAt(adminTermDto.getCreatedAt())
                .updatedAt(adminTermDto.getUpdatedAt())
                .termVersionId(adminTermDto.getTermVersionId())
                .version(adminTermDto.getVersion())
                .isCurrent(adminTermDto.getIsCurrent())
                .content(adminTermDto.getContent())
                .effectiveDateTime(adminTermDto.getEffectiveDateTime())
                .expiryDateTime(adminTermDto.getExpiryDateTime())
                .termVersionCreatedAt(adminTermDto.getTermVersionCreatedAt())
                .termVersionUpdatedAt(adminTermDto.getTermVersionUpdatedAt())
                .build();
    }
}
