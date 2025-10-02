package com.f1v3.reservation.common.domain.user.enums;

import lombok.Getter;

/**
 * 회원의 권한을 나타내는 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum UserRole {

    USER("일반 유저"),
    SUPPLIER("공급 유저"),
    ADMIN("관리자"),
    ;

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
