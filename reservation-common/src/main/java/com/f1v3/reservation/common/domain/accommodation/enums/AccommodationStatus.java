package com.f1v3.reservation.common.domain.accommodation.enums;

import lombok.Getter;

/**
 * 숙소 상태를 나타내는 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum AccommodationStatus {

    NONE("초기 상태"),
    PENDING("승인 대기 상태"),
    APPROVED("승인 완료 상태"),
    REJECTED("승인 반려 상태"),
    SUSPENDED("운영 중지 상태");

    private final String description;

    AccommodationStatus(String description) {
        this.description = description;
    }


}
