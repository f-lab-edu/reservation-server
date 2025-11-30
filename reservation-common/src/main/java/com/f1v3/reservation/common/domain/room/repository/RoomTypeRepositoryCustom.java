package com.f1v3.reservation.common.domain.room.repository;

import com.f1v3.reservation.common.domain.room.dto.RoomResponseDto;
import com.f1v3.reservation.common.domain.room.dto.RoomTypeSummaryDto;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 객실 타입 Querydsl 레포지토리 커스텀 인터페이스
 *
 * @author Seungjo, Jeong
 */
@NoRepositoryBean
public interface RoomTypeRepositoryCustom {

    List<RoomResponseDto> findRoomsByAccommodationId(Long accommodationId);

    List<RoomTypeSummaryDto> findAllByAccommodationId(Long accommodationId);
}
