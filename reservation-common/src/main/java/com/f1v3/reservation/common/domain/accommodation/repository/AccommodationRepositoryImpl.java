package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.QAccommodation;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 숙소 Repository Querydsl 구현체
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAccommodation accommodation = QAccommodation.accommodation;

    @Override
    public List<SearchAccommodationDto> findContainingNameOrRegion(String keyword) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SearchAccommodationDto.class,
                                accommodation.name,
                                accommodation.description,
                                accommodation.address,
                                accommodation.contactNumber
                        ))
                .from(accommodation)
                .where(
                        accommodation.name.contains(keyword)
                                .or(accommodation.address.contains(keyword))
                ).fetch();
    }
}
