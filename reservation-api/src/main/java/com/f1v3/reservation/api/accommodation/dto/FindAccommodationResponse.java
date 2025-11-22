package com.f1v3.reservation.api.accommodation.dto;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;

import java.math.BigDecimal;
import java.util.List;

/**
 * 숙소 상세 조회 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record FindAccommodationResponse(
        Long id,
        String name,
        String address,
        String description,
        String thumbnail,
        List<RoomTypeDetail> roomTypes
) {

    public static FindAccommodationResponse from(
            Accommodation accommodation,
            List<RoomTypeAvailabilityDto> rooms
    ) {

        List<RoomTypeDetail> roomDetails = rooms.stream()
                .map(room -> new RoomTypeDetail(
                        room.roomTypeId(),
                        room.roomTypeName(),
                        room.roomTypeDescription(),
                        room.standardCapacity(),
                        room.maxCapacity(),
                        room.basePrice(),
                        room.roomTypeThumbnail(),
                        room.totalRoomCount(),
                        room.availableCount(),
                        room.isAvailable()
                ))
                .toList();

        return new FindAccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getDescription(),
                accommodation.getThumbnail(),
                roomDetails
        );
    }

    public record RoomTypeDetail(
            Long id,
            String name,
            String description,
            int standardCapacity,
            int maxCapacity,
            BigDecimal basePrice,
            String thumbnail,
            int totalRoomCount,
            int availableRoomCount,
            boolean isAvailable

    ) {
    }
}
