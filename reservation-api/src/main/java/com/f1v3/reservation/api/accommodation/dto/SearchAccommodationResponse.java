package com.f1v3.reservation.api.accommodation.dto;

import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;

/**
 * 숙소 검색 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record SearchAccommodationResponse(
        String name,
        String description,
        String address,
        String contactNumber
) {

    public static SearchAccommodationResponse from(SearchAccommodationDto searchAccommodationDto) {
        return new SearchAccommodationResponse(
                searchAccommodationDto.name(),
                searchAccommodationDto.description(),
                searchAccommodationDto.address(),
                searchAccommodationDto.contactNumber()
        );
    }
}
