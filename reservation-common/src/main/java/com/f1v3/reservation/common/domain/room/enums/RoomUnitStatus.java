package com.f1v3.reservation.common.domain.room.enums;

import lombok.Getter;

@Getter
public enum RoomUnitStatus {
    AVAILABLE("사용 가능"),
    OCCUPIED("투숙 중"),
    MAINTENANCE("수리/정비 중"),
    CLEANING("청소 중"),
    OUT_OF_SERVICE("사용 불가");

    private final String description;

    RoomUnitStatus(String description) {
        this.description = description;
    }
}