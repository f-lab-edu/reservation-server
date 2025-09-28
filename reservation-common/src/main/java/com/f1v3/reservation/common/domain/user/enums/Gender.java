package com.f1v3.reservation.common.domain.user.enums;

import lombok.Getter;

/**
 * 회원의 성별을 나타내는 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum Gender {

    M("남성"),
    F("여성");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}
