package com.f1v3.reservation.common.domain.accommodation.dto;

import java.math.BigDecimal;

/**
 * 숙소 검색 DTO
 *
 * @author Seungjo, Jeong
 */
public record SearchAccommodationDto(
        Long id,
        String name,
        String address,
        String thumbnail,
        BigDecimal minPrice
) {
}
