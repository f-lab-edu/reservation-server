package com.f1v3.reservation.api.term.dto;

import com.f1v3.reservation.common.domain.term.dto.CurrentTermDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 약관 응답 DTO
 *
 * @author Seungjo, Jeong
 */
@Getter
public class TermResponse {
    private final Long termId;
    private final String termCode;
    private final Integer version;
    private final String title;
    private final String type;
    private final String content;
    private final Integer displayOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private TermResponse(Long termId, String termCode, Integer version, String title, String type, String content, Integer displayOrder) {
        this.termId = termId;
        this.termCode = termCode;
        this.version = version;
        this.title = title;
        this.type = type;
        this.content = content;
        this.displayOrder = displayOrder;
    }

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