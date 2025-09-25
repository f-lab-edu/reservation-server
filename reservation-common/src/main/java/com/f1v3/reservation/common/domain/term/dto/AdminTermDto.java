package com.f1v3.reservation.common.domain.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 약관 DTO (Term + TermVersion 정보 포함)
 *
 * @author Seungjo, Jeong
 */
@Getter
public class AdminTermDto {

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

    public AdminTermDto(Long termId, TermCode termCode, String title, TermType type, Integer displayOrder, TermStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                        Long termVersionId, Integer version, Boolean isCurrent, String content, LocalDateTime effectiveDateTime, LocalDateTime expiryDateTime, LocalDateTime termVersionCreatedAt, LocalDateTime termVersionUpdatedAt) {
        this.termId = termId;
        this.termCode = termCode.name();
        this.title = title;
        this.type = type.name();
        this.displayOrder = displayOrder;
        this.status = status.name();
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
}
