package com.f1v3.reservation.common.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 가계약(Reservation Hold) 도메인 객체 (엔티티가 아닌 도메인 객체)
 *
 * @author Seungjo, Jeong
 */
public record ReservationHold(
        String holdId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        Long userId,
        String holdRequestKey,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
}
