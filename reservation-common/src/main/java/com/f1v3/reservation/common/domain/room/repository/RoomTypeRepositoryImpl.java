package com.f1v3.reservation.common.domain.room.repository;

import com.f1v3.reservation.common.domain.room.QRoomType;
import com.f1v3.reservation.common.domain.room.QRoomUnit;
import com.f1v3.reservation.common.domain.room.dto.RoomResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
public class RoomTypeRepositoryImpl implements RoomTypeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QRoomType roomType = QRoomType.roomType;
    private final QRoomUnit roomUnit = QRoomUnit.roomUnit;

    @Override
    public List<RoomResponseDto> findRoomsByAccommodationId(Long accommodationId) {
        return queryFactory
                .from(roomType)
                .leftJoin(roomUnit).on(roomUnit.roomType.id.eq(roomType.id))
                .where(roomType.accommodation.id.eq(accommodationId))
                .transform(
                        groupBy(roomType.id).list(
                                Projections.constructor(
                                        RoomResponseDto.class,
                                        roomType.id,
                                        roomType.name,
                                        roomType.description,
                                        roomType.standardCapacity,
                                        roomType.maxCapacity,
                                        roomType.totalRoomCount,
                                        roomType.basePrice,
                                        roomType.thumbnail,
                                        list(
                                                Projections.constructor(
                                                        RoomResponseDto.RoomUnitDto.class,
                                                        roomUnit.id,
                                                        roomUnit.roomNumber,
                                                        roomUnit.status
                                                )
                                        )
                                )
                        )
                );
    }
}
