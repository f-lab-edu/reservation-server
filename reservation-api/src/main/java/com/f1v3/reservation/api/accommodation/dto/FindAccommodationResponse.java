package com.f1v3.reservation.api.accommodation.dto;

import com.f1v3.reservation.api.reservation.dto.AvailabilityRoomResponse;
import com.f1v3.reservation.common.domain.accommodation.dto.FindAccommodationDto;
import com.f1v3.reservation.common.domain.accommodation.dto.FindAccommodationRoomDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            FindAccommodationDto dto,
            List<AvailabilityRoomResponse> reservedRooms
    ) {

        Map<Long/* 객실 타입 ID */, Long/* 예약 건수 */> reservedCountMap = reservedRooms.stream()
                .collect(Collectors.toMap(
                        AvailabilityRoomResponse::roomTypeId,
                        AvailabilityRoomResponse::reservedCount
                ));

        List<RoomTypeDetail> rooms = dto.rooms().stream()
                .map(room -> toRoomTypeDetail(room, reservedCountMap))
                .toList();

        return new FindAccommodationResponse(
                dto.accommodationId(),
                dto.accommodationName(),
                dto.accommodationAddress(),
                dto.accommodationDescription(),
                dto.accommodationThumbnail(),
                rooms
        );
    }

    private static RoomTypeDetail toRoomTypeDetail(
            FindAccommodationRoomDto room,
            Map<Long, Long> reservedCountMap
    ) {
        long reservedCount = reservedCountMap.getOrDefault(room.roomTypeId(), 0L);
        int availableRoomCount = (int) (room.totalRoomCount() - reservedCount);

        return new RoomTypeDetail(
                room.roomTypeId(),
                room.roomTypeName(),
                room.roomTypeDescription(),
                room.standardCapacity(),
                room.maxCapacity(),
                room.basePrice(),
                room.roomTypeThumbnail(),
                room.totalRoomCount(),
                availableRoomCount,
                availableRoomCount > 0
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
            boolean available

    ) {
    }
}
