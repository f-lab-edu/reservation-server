package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.domain.accommodation.repository.SearchAccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    private final SearchAccommodationRepository searchAccommodationRepository;

    public List<SearchAccommodationResponse> search(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            int capacity,
            Pageable pageable
    ) {

        return searchAccommodationRepository.search(keyword, checkIn, checkOut, capacity, pageable.getPageNumber(), pageable.getPageSize()).stream()
                .map(SearchAccommodationResponse::from)
                .toList();
    }
}
