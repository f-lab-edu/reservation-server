package com.f1v3.reservation.common.domain.term.dto;

import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import lombok.Getter;

/**
 * 현재 활성화된 약관 정보 DTO
 * Repository 레이어에서 복잡한 조인 쿼리 결과를 담기 위한 DTO
 *
 * @author Seungjo, Jeong
 */
@Getter
public class CurrentTermDto {

    private final Long termId;
    private final String termCode;
    private final String title;
    private final String type;
    private final Integer displayOrder;
    private final Integer version;
    private final String content;

    public CurrentTermDto(Long termId, TermCode termCode, String title,
                          TermType termType, Integer displayOrder, Integer version, String content) {
        this.termId = termId;
        this.termCode = termCode.name();
        this.title = title;
        this.type = termType.name();
        this.displayOrder = displayOrder;
        this.version = version;
        this.content = content;
    }
}
