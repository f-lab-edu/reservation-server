package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.api.response.PagedResponse;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * 숙소 커스텀 Repository 인터페이스 (QueryDSL 사용)
 *
 * @author Seungjo, Jeong
 */
public interface AccommodationRepositoryCustom {

    PagedResponse<SearchAccommodationDto> search(
            LocalDate checkIn,
            LocalDate checkOut,
            int capacity,
            Pageable pageable
    );
}
