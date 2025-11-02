package com.f1v3.reservation.api.room.dto;

import com.f1v3.reservation.common.domain.room.RoomType;

import java.math.BigDecimal;

/**
 * 객실 타입 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomTypeResponse(
        Long id,
        String name,
        String description,
        int standardCapacity,
        int maxCapacity,
        int totalRoomCount,
        BigDecimal basePrice,
        String thumbnail
) {

    public static RoomTypeResponse from(RoomType roomType) {
        return new RoomTypeResponse(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getStandardCapacity(),
                roomType.getMaxCapacity(),
                roomType.getTotalRoomCount(),
                roomType.getBasePrice(),
                roomType.getThumbnail()
        );
    }
}
