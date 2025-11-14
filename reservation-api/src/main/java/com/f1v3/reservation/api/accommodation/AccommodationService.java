package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.FindAccommodationResponse;
import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.api.reservation.ReservationService;
import com.f1v3.reservation.api.reservation.dto.AvailabilityRoomResponse;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.PageInfo;
import com.f1v3.reservation.common.api.response.PagedResponse;
import com.f1v3.reservation.common.domain.accommodation.dto.FindAccommodationDto;
import com.f1v3.reservation.common.domain.accommodation.dto.FindAccommodationRoomDto;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 사용자용 숙소 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final ReservationService reservationService;

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

        if (!accommodationRepository.existsById(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        if (!accommodationRepository.isAccommodationVisible(accommodationId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_VISIBILITY_DISABLED, log::info);
        }

        FindAccommodationDto accommodation = accommodationRepository.findAccommodationWithRooms(accommodationId);

        List<Long> roomTypeIds = accommodation.rooms().stream()
                .map(FindAccommodationRoomDto::roomTypeId)
                .toList();

        List<AvailabilityRoomResponse> reservedRooms = reservationService.countReservations(roomTypeIds, checkIn, checkOut);

        return FindAccommodationResponse.from(accommodation, reservedRooms);
    }

    private void validatePeriod(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            Map<String, Object> parameters = Map.of(
                    "checkIn", checkIn,
                    "checkOut", checkOut
            );

            throw new ReservationException(ErrorCode.INVALID_REQUEST_PARAMETER, log::info, parameters);
        }
    }
}
