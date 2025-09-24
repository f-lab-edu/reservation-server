package com.f1v3.reservation.common.domain.term.enums;

import lombok.Getter;

/**
 * 약관 상태 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum TermStatus {
    ACTIVE("활성화 상태"),
    INACTIVE("비활성화 상태"),
    ;

    private final String description;

    TermStatus(String description) {
        this.description = description;
    }
}
