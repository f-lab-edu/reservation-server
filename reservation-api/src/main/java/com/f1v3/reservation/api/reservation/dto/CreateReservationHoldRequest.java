package com.f1v3.reservation.api.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 임시 예약 생성 요청
 *
 * @author Seungjo, Jeong
 */
public record CreateReservationHoldRequest(
        @NotNull(message = "객실 타입 ID를 입력해주세요.")
        Long roomTypeId,

        @NotNull(message = "체크인 날짜를 입력해주세요.")
        LocalDate checkIn,

        @NotNull(message = "체크아웃 날짜를 입력해주세요.")
        LocalDate checkOut,

        @NotNull(message = "인원 수를 입력해주세요.")
        @Min(value = 1, message = "최소 1명 이상의 예약 인원을 입력해주세요.")
        Integer capacity
) {
}
