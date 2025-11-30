package com.f1v3.reservation.common.domain.accommodation.dto;

import com.f1v3.reservation.common.domain.room.dto.RoomTypeSummaryDto;

import java.util.List;

/**
 * 숙소 상세 조회 DTO
 *
 * @author Seungjo, Jeong
 */
public record FindAccommodationDto(
        Long accommodationId,
        String accommodationName,
        String accommodationAddress,
        String accommodationDescription,
        String accommodationThumbnail,
        List<RoomTypeSummaryDto> rooms
) {
}
