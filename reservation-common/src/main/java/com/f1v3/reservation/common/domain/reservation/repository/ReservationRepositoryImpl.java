package com.f1v3.reservation.common.domain.reservation.repository;

import com.f1v3.reservation.common.domain.reservation.QReservation;
import com.f1v3.reservation.common.domain.reservation.dto.AvailabilityRoomDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 Repository QueryDSL 구현체
 *
 * @author Seungjo, Jeong
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QReservation reservation = QReservation.reservation;

    @Override
    public List<AvailabilityRoomDto> countOverlappingReservations(
            List<Long> roomTypeIds,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (roomTypeIds == null || roomTypeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(
                        Projections.constructor(
                                AvailabilityRoomDto.class,
                                reservation.roomTypeId,
                                reservation.id.count()
                        )
                )
                .from(reservation)
                .where(
                        reservation.roomTypeId.in(roomTypeIds),
                        reservation.checkIn.lt(checkOut),
                        reservation.checkOut.gt(checkIn)
                )
                .groupBy(reservation.roomTypeId)
                .fetch();
    }
}
