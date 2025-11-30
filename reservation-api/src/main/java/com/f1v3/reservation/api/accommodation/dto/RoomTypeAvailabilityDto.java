package com.f1v3.reservation.api.accommodation.dto;

import com.f1v3.reservation.common.domain.reservation.dto.AvailabilityRoomDto;
import com.f1v3.reservation.common.domain.room.dto.RoomTypeSummaryDto;

import java.math.BigDecimal;

/**
 * 객실 타입 가용 여부 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomTypeAvailabilityDto(
        Long roomTypeId,
        String roomTypeName,
        String roomTypeDescription,
        Integer standardCapacity,
        Integer maxCapacity,
        BigDecimal basePrice,
        String roomTypeThumbnail,
        Integer totalRoomCount,
        Long reservedCount,
        Integer availableCount,
        boolean isAvailable
) {

    public static RoomTypeAvailabilityDto of(
            RoomTypeSummaryDto room,
            AvailabilityRoomDto reservedRoom
    ) {
        long reservedCount = reservedRoom == null ? 0L : reservedRoom.reservedCount();
        int totalRoomCount = room.totalRoomCount() == null ? 0 : room.totalRoomCount();
        long available = totalRoomCount - reservedCount;
        int availableRoomCount = (int) Math.max(available, 0);
        boolean isAvailable = availableRoomCount > 0;

        return new RoomTypeAvailabilityDto(
                room.roomTypeId(),
                room.roomTypeName(),
                room.roomTypeDescription(),
                room.standardCapacity(),
                room.maxCapacity(),
                room.basePrice(),
                room.roomTypeThumbnail(),
                totalRoomCount,
                reservedCount,
                availableRoomCount,
                isAvailable
        );
    }
}
