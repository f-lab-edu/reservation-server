package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.api.response.PageInfo;
import com.f1v3.reservation.common.api.response.PagedResponse;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 사용자용 숙소 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

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
}
