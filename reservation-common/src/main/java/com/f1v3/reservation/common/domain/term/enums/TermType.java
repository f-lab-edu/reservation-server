package com.f1v3.reservation.common.domain.term.enums;

import lombok.Getter;

/**
 * 약관 타입 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum TermType {

    REQUIRED("필수 약관"),
    OPTIONAL("선택 약관"),
    ;

    private final String description;

    TermType(String description) {
        this.description = description;
    }
}
