package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.api.response.PageInfo;
import com.f1v3.reservation.common.api.response.PagedResponse;
import com.f1v3.reservation.common.domain.accommodation.QAccommodation;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import com.f1v3.reservation.common.domain.reservation.QReservation;
import com.f1v3.reservation.common.domain.room.QRoomType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 숙소 Querydsl Repository 구현체
 *
 * @author Seungjo, Jeong
 */
@Repository
@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QAccommodation accommodation = QAccommodation.accommodation;
    private static final QRoomType roomType = QRoomType.roomType;
    private static final QReservation reservation = QReservation.reservation;

    @Override
    public PagedResponse<SearchAccommodationDto> search(
            LocalDate checkIn,
            LocalDate checkOut,
            int capacity,
            Pageable pageable
    ) {

        BooleanExpression[] conditions = Stream.of(
                        accommodation.isVisible.isTrue(),
                        roomType.standardCapacity.loe(capacity).and(roomType.maxCapacity.goe(capacity))
                )
                .filter(Objects::nonNull)
                .toArray(BooleanExpression[]::new);

        NumberExpression<Long> reservedRooms = reservation.id.count();
        NumberExpression<Long> totalRooms = roomType.totalRoomCount.castToNum(Long.class);

        // 콘텐츠 조회
        List<SearchAccommodationDto> content = queryFactory
                .select(
                        Projections.constructor(
                                SearchAccommodationDto.class,
                                accommodation.id,
                                accommodation.name,
                                accommodation.address,
                                accommodation.thumbnail,
                                roomType.basePrice.min(),
                                Expressions.nullExpression(Double.class)
                        )
                )
                .from(roomType)
                .join(roomType.accommodation, accommodation)
                .leftJoin(reservation)
                .on(
                        reservation.roomTypeId.eq(roomType.id),
                        reservation.checkIn.lt(checkOut),
                        reservation.checkOut.gt(checkIn)
                )
                .where(conditions)
                .groupBy(
                        accommodation.id,
                        accommodation.name,
                        accommodation.address,
                        accommodation.thumbnail
                )
                .having(reservedRooms.lt(totalRooms))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 개수 조회
        long totalElements = queryFactory
                .select(accommodation.id)
                .from(roomType)
                .join(roomType.accommodation, accommodation)
                .leftJoin(reservation)
                .on(
                        reservation.roomTypeId.eq(roomType.id),
                        reservation.checkIn.lt(checkOut),
                        reservation.checkOut.gt(checkIn)
                )
                .where(conditions)
                .groupBy(
                        accommodation.id,
                        accommodation.name,
                        accommodation.address,
                        accommodation.thumbnail
                )
                .having(reservedRooms.lt(totalRooms))
                .fetch()
                .size();

        PageInfo pageInfo = PageInfo.of(pageable, totalElements);
        return PagedResponse.of(content, pageInfo);
    }
}
