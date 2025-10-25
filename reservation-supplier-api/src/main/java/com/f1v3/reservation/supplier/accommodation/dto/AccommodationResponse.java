package com.f1v3.reservation.supplier.accommodation.dto;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;

/**
 * 숙소 조회 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record AccommodationResponse(
        Long id,
        String name,
        String description,
        String address,
        String contactNumber,
        String thumbnail
) {
    public static AccommodationResponse from(Accommodation accommodation) {
        return new AccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getDescription(),
                accommodation.getAddress(),
                accommodation.getContactNumber(),
                accommodation.getThumbnail()
        );
    }
}
