package com.f1v3.reservation.common.domain.accommodation.dto;

/**
 * 숙소 검색 DTO
 *
 * @author Seungjo, Jeong
 */
public record SearchAccommodationDto(
        String name,
        String description,
        String address,
        String contactNumber
) {
}
