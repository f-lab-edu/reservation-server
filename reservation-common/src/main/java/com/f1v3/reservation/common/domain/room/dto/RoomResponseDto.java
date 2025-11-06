package com.f1v3.reservation.common.domain.room.dto;

import com.f1v3.reservation.common.domain.room.enums.RoomUnitStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * 객실 조회 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomResponseDto(
        Long roomTypeId,
        String name,
        String description,
        int standardCapacity,
        int maxCapacity,
        int totalRoomCount,
        BigDecimal basePrice,
        String thumbnail,
        List<RoomUnitDto> roomUnits
) {

    public record RoomUnitDto(
            Long roomId,
            String roomNumber,
            RoomUnitStatus status
    ) {

    }
}
