package com.f1v3.reservation.common.domain.reservation.repository;

import com.f1v3.reservation.common.domain.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 예약 정보 조회 Repository
 *
 * @author Seungjo, Jeong
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        ReservationRepositoryCustom {
}
