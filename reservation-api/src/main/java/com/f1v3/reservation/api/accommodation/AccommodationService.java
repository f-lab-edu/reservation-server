package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.FindAccommodationResponse;
import com.f1v3.reservation.api.accommodation.dto.RoomTypeAvailabilityDto;
import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.PageInfo;
import com.f1v3.reservation.common.api.response.PagedResponse;
import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import com.f1v3.reservation.common.domain.reservation.dto.AvailabilityRoomDto;
import com.f1v3.reservation.common.domain.reservation.repository.ReservationRepository;
import com.f1v3.reservation.common.domain.room.dto.RoomTypeSummaryDto;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 사용자용 숙소 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRepository reservationRepository;

    public PagedResponse<SearchAccommodationResponse> search(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            int capacity,
            Pageable pageable
    ) {
        if (StringUtils.hasText(keyword)) {
            Page<SearchAccommodationDto> response = accommodationRepository.searchFullText(
                    keyword.trim(),
                    checkIn,
                    checkOut,
                    capacity,
                    pageable
            );

            List<SearchAccommodationResponse> content = response.getContent().stream()
                    .map(SearchAccommodationResponse::from)
                    .toList();

            return PagedResponse.of(content, PageInfo.of(pageable, response.getTotalElements()));
        }

        return accommodationRepository.search(checkIn, checkOut, capacity, pageable)
                .map(SearchAccommodationResponse::from);
    }

    /**
     * 숙소 상세 조회 (객실 타입 정보와 예약 가능 여부 포함)
     */
    public FindAccommodationResponse findAccommodation(Long accommodationId, LocalDate checkIn, LocalDate checkOut, int capacity) {
        validatePeriod(checkIn, checkOut);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .filter(Accommodation::isVisible) /* 예외 메시지 분리가 필요할까? */
                .orElseThrow(() -> new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info));

        List<RoomTypeSummaryDto> roomTypes = roomTypeRepository.findAllByAccommodationId(accommodationId);

        List<Long> roomTypeIds = roomTypes.stream()
                .map(RoomTypeSummaryDto::roomTypeId)
                .toList();

        // todo: ReservationHold를 고려해서 재고 집계를 해야 함. (Redis 조회 + DB 조회 병합)
        Map<Long, AvailabilityRoomDto> reservedMap = reservationRepository.countOverlappingReservations(roomTypeIds, checkIn, checkOut)
                .stream()
                .collect(Collectors.toMap(
                        AvailabilityRoomDto::roomTypeId,
                        Function.identity())
                );

        // 객실 타입 ID : 객실 정보 + 예약 가능 여부 매핑
        List<RoomTypeAvailabilityDto> roomTypeAvailabilities = roomTypes.stream()
                .map(room -> {
                    AvailabilityRoomDto reserved = reservedMap.get(room.roomTypeId());
                    return RoomTypeAvailabilityDto.of(room, reserved);
                })
                .toList();

        return FindAccommodationResponse.from(accommodation, roomTypeAvailabilities);
    }

    private void validatePeriod(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("checkIn", checkIn);
            parameters.put("checkOut", checkOut);
            throw new ReservationException(ErrorCode.INVALID_REQUEST_PARAMETER, log::info, parameters);
        }
    }
}
