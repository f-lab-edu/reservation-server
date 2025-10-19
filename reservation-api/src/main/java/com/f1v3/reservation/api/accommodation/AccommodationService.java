package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<SearchAccommodationResponse> search(String keyword) {
        return accommodationRepository.findContainingNameOrRegion(keyword).stream()
                .map(SearchAccommodationResponse::from)
                .toList();
    }

    public List<SearchAccommodationResponse> searchV2(String keyword) {
        return accommodationRepository.fullTextSearchByKeyword(keyword).stream()
                .map(accommodation -> new SearchAccommodationResponse(
                        accommodation.getName(),
                        accommodation.getDescription(),
                        accommodation.getAddress(),
                        accommodation.getContactNumber()
                ))
                .toList();
    }
}
