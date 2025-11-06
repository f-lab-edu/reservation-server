package com.f1v3.reservation.supplier.room;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.domain.room.RoomUnit;
import com.f1v3.reservation.common.domain.room.dto.RoomResponseDto;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeRepository;
import com.f1v3.reservation.common.domain.room.repository.RoomUnitRepository;
import com.f1v3.reservation.supplier.room.dto.CreateRoomTypeRequest;
import com.f1v3.reservation.supplier.room.dto.CreateRoomTypeResponse;
import com.f1v3.reservation.supplier.room.dto.RoomTypeResponse;
import com.f1v3.reservation.supplier.room.dto.UpdateRoomTypeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 객실 타입 서비스 - 공급자용
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final AccommodationRepository accommodationRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomUnitRepository roomUnitRepository;

    @Transactional
    public CreateRoomTypeResponse create(Long accommodationId, CreateRoomTypeRequest request) {

        validateCapacity(request);
        validateRoomCount(request);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info));

        RoomType roomType = RoomType.builder()
                .accommodation(accommodation)
                .name(request.name())
                .description(request.description())
                .standardCapacity(request.standardCapacity())
                .maxCapacity(request.maxCapacity())
                .totalRoomCount(request.totalRoomCount())
                .basePrice(request.basePrice())
                .thumbnail(request.thumbnail())
                .build();

        RoomType savedRoomType = roomTypeRepository.save(roomType);

        for (String roomNumber : request.roomNumbers()) {
            RoomUnit roomUnit = RoomUnit.builder()
                    .roomType(roomType)
                    .roomNumber(roomNumber)
                    .build();

            roomUnitRepository.save(roomUnit);
        }

        return new CreateRoomTypeResponse(savedRoomType.getId());
    }

    public List<RoomTypeResponse> getRoomTypes(Long accommodationId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        // RoomUnit 조회도 같이 해줘야 함
        List<RoomResponseDto> roomTypes = roomTypeRepository.findRoomsByAccommodationId(accommodationId);
//        List<RoomType> roomTypes = roomTypeRepository.findByAccommodationId(accommodationId);

        return roomTypes.stream()
                .map(RoomTypeResponse::from)
                .toList();
    }

    public RoomTypeResponse getRoomType(Long accommodationId, Long roomTypeId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ROOM_TYPE_NOT_FOUND, log::info));

        return null;
    }

    @Transactional
    public void update(Long accommodationId, Long roomTypeId, UpdateRoomTypeRequest request) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ROOM_TYPE_NOT_FOUND, log::info));

        roomType.updateDetails(
                request.name(),
                request.description(),
                request.standardCapacity(),
                request.maxCapacity(),
                request.basePrice(),
                request.totalRoomCount(),
                request.thumbnail()
        );
    }

    @Transactional
    public void delete(Long accommodationId, Long roomTypeId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ReservationException(ErrorCode.ROOM_TYPE_NOT_FOUND, log::info);
        }

        // Soft Delete 처리를 해야할까?
        roomTypeRepository.deleteById(roomTypeId);
    }

    private void validateRoomCount(CreateRoomTypeRequest request) {
        if (request.roomNumbers().size() != request.totalRoomCount()) {
            Map<String, Object> details = new HashMap<>();
            details.put("roomNumbersCount", request.roomNumbers().size());
            details.put("totalRoomCount", request.totalRoomCount());
            throw new ReservationException(ErrorCode.INVALID_REQUEST_PARAMETER, log::info, details);
        }
    }

    private void validateCapacity(CreateRoomTypeRequest request) {
        if (request.standardCapacity() > request.maxCapacity()) {
            Map<String, Object> details = new HashMap<>();
            details.put("standardCapacity", request.standardCapacity());
            details.put("maxCapacity", request.maxCapacity());
            throw new ReservationException(ErrorCode.INVALID_REQUEST_PARAMETER, log::info, details);
        }
    }

}