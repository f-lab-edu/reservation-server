package com.f1v3.reservation.api.accommodation.dto;

import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;

import java.math.BigDecimal;

/**
 * 숙소 검색 응답 DTO (예약 가능 여부 포함)
 *
 * @author Seungjo, Jeong
 */
public record SearchAccommodationResponse(
        Long id,
        String name,
        String address,
        String thumbnail,
        BigDecimal minPrice
) {

    public static SearchAccommodationResponse from(SearchAccommodationDto dto) {
        return new SearchAccommodationResponse(
                dto.id(),
                dto.name(),
                dto.address(),
                dto.thumbnail(),
                dto.minPrice()
        );
    }

}
