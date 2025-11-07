package com.f1v3.reservation.supplier.room.dto;

import com.f1v3.reservation.common.domain.room.enums.RoomUnitStatus;

/**
 * 객실 유닛 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomUnitResponse(
        Long roomUnitId,
        String roomNumber,
        RoomUnitStatus status
) {
}
