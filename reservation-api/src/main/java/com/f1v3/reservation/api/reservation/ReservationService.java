package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.AvailabilityRoomResponse;
import com.f1v3.reservation.api.reservation.dto.ConfirmReservationHoldResponse;
import com.f1v3.reservation.common.domain.reservation.Reservation;
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

        // fixme: ReservationHold 고려해서 재고를 집계해줘야 함.
        //        즉, 임시 예약 + 실제 예약 정보를 합산해서 집계해야 함.

        return reservationRepository.countOverlappingReservations(roomTypeId, checkIn, checkOut)
                .stream()
                .map(AvailabilityRoomResponse::from)
                .toList();
    }

    public void confirmReservation(Long userId, ConfirmReservationHoldResponse response) {
        for (int i = 0; i < response.quantity(); i++) {
            Reservation reservation = Reservation.builder()
                    .userId(userId)
                    .roomTypeId(response.roomTypeId())
                    .checkIn(response.checkIn())
                    .checkOut(response.checkOut())
                    .build();

            reservationRepository.save(reservation);
        }
    }
}
