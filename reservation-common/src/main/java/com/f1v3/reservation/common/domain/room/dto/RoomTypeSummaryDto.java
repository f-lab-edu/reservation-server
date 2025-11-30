package com.f1v3.reservation.common.domain.room.dto;

import java.math.BigDecimal;

/**
 * 객실 타입 요약 조회 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomTypeSummaryDto(
        Long roomTypeId,
        String roomTypeName,
        String roomTypeDescription,
        Integer standardCapacity,
        Integer maxCapacity,
        BigDecimal basePrice,
        String roomTypeThumbnail,
        Integer totalRoomCount
) {
}
