package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.AccommodationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 숙소 상태 변경 이력 엔티티의 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface AccommodationStatusHistoryRepository extends JpaRepository<AccommodationStatusHistory, Long> {
}
