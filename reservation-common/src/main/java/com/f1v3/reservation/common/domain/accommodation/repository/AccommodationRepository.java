package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 숙소 엔티티의 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}
