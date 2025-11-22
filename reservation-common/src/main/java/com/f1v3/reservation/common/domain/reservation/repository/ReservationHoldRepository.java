package com.f1v3.reservation.common.domain.reservation.repository;

import com.f1v3.reservation.common.domain.reservation.ReservationHold;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 임시 예약 JPA 레포지토리
 *
 * @author Seungjo, Jeong
 */
public interface ReservationHoldRepository extends JpaRepository<ReservationHold, String> {

}
