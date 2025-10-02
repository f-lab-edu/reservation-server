package com.f1v3.reservation.common.domain.user.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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
    private static final Map<String, Gender> stringToEnum = new HashMap<>();

    static {
        for (Gender gender : values()) {
            stringToEnum.put(gender.name(), gender);
        }
    }

    Gender(String description) {
        this.description = description;
    }

    public static Gender findBy(String value) {
        return stringToEnum.get(value);
    }
}
