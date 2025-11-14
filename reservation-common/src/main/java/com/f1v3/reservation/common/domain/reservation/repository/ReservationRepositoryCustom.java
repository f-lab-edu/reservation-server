package com.f1v3.reservation.common.domain.reservation.repository;

import com.f1v3.reservation.common.domain.reservation.dto.AvailabilityRoomDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 조회용 커스텀 Repository
 *
 * @author Seungjo, Jeong
 */
public interface ReservationRepositoryCustom {

    List<AvailabilityRoomDto> countOverlappingReservations(
            List<Long> roomTypeIds,
            LocalDate checkIn,
            LocalDate checkOut
    );
}
