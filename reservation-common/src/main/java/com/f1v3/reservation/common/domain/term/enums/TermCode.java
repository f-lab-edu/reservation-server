package com.f1v3.reservation.common.domain.term.enums;

import lombok.Getter;

/**
 * 약관 코드 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum TermCode {

    TERM_SERVICE("서비스 이용약관"),
    TERM_PRIVACY("개인정보 처리방침"),
    TERM_MARKETING("마케팅 수신 동의"),
    TERM_AGE("만 14세 이상 동의"),
    TERM_OF_PERSONAL_INFORMATION("개인정보 수집 및 이용 동의")
    ;

    private final String description;

    TermCode(String description) {
        this.description = description;
    }
}
