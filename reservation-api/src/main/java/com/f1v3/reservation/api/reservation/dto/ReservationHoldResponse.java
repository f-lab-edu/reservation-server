package com.f1v3.reservation.api.reservation.dto;

import java.time.LocalDateTime;

/**
 * 가계약 생성 응답
 *
 * @author Seungjo, Jeong
 */
public record ReservationHoldResponse(
        String holdId,
        LocalDateTime expiredAt
) {
}
