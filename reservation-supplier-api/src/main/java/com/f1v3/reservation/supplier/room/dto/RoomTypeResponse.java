package com.f1v3.reservation.supplier.room.dto;

import com.f1v3.reservation.common.domain.room.dto.RoomResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 객실 타입 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record RoomTypeResponse(
        Long id,
        String name,
        String description,
        int standardCapacity,
        int maxCapacity,
        int totalRoomCount,
        BigDecimal basePrice,
        String thumbnail,
        List<RoomUnitResponse> roomUnits
) {

    public static RoomTypeResponse from(RoomResponseDto responseDto) {
        return new RoomTypeResponse(
                responseDto.roomTypeId(),
                responseDto.name(),
                responseDto.description(),
                responseDto.standardCapacity(),
                responseDto.maxCapacity(),
                responseDto.totalRoomCount(),
                responseDto.basePrice(),
                responseDto.thumbnail(),
                responseDto.roomUnits().stream()
                        .map(roomUnitDto -> new RoomUnitResponse(
                                roomUnitDto.roomId(),
                                roomUnitDto.roomNumber(),
                                roomUnitDto.status()
                        ))
                        .toList()
        );
    }
}
