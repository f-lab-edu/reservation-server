package com.f1v3.reservation.common.domain.room.repository;

import com.f1v3.reservation.common.domain.room.RoomUnit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 객실 엔티티의 JPA 레포지토리
 *
 * @author Seungjo, Jeong
 */
public interface RoomUnitRepository extends JpaRepository<RoomUnit, Long> {
}
