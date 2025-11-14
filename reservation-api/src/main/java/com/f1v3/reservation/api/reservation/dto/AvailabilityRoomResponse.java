package com.f1v3.reservation.api.reservation.dto;

import com.f1v3.reservation.common.domain.reservation.dto.AvailabilityRoomDto;

/**
 * 객실 타입별 예약 건수 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record AvailabilityRoomResponse(
        Long roomTypeId,
        Long reservedCount
) {
    public static AvailabilityRoomResponse from(AvailabilityRoomDto dto) {
        return new AvailabilityRoomResponse(dto.roomTypeId(), dto.reservedCount());
    }
}
