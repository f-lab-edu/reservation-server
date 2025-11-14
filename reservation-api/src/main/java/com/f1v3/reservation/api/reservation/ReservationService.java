package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.AvailabilityRoomResponse;
import com.f1v3.reservation.common.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 서비스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public List<AvailabilityRoomResponse> countReservations(List<Long> roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return reservationRepository.countOverlappingReservations(roomTypeId, checkIn, checkOut)
                .stream()
                .map(AvailabilityRoomResponse::from)
                .toList();
    }
}
