package com.f1v3.reservation.common.domain.room.repository;

import com.f1v3.reservation.common.domain.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 객실 타입 엔티티의 JPA 레포지토리
 *
 * @author Seungjo, Jeong
 */
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByAccommodationId(Long accommodationId);
}
