package com.f1v3.reservation.common.domain.reservation.dto;

/**
 * 객실 타입별 예약 건수 DTO
 *
 * @author Seungjo, Jeong
 */
public record AvailabilityRoomDto(
        Long roomTypeId,
        Long reservedCount
) {
}
