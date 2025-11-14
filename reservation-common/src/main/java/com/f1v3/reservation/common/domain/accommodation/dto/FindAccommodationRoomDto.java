package com.f1v3.reservation.common.domain.accommodation.dto;

import java.math.BigDecimal;

/**
 * 숙소 객실 조회 DTO
 *
 * @author Seungjo, Jeong
 */
public record FindAccommodationRoomDto(
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