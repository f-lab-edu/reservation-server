package com.f1v3.reservation.api.reservation.dto;

import java.time.LocalDate;

/**
 * 임시 예약 확정 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record ConfirmReservationHoldResponse(
        String holdKey,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int quantity
) {
}
