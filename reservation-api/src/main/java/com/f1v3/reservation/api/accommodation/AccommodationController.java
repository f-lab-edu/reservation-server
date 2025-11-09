package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 사용자용 숙소 컨트롤러 클래스
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    /**
     * 숙소 검색 API
     *
     * @param keyword 검색 키워드 (숙소명과 주소에 대해 부분 일치 검색)
     * @param checkIn 체크인 날짜
     * @param checkOut 체크아웃 날짜
     * @param capacity 숙박 인원
     */
    @GetMapping("/search")
    public ApiResponse<List<SearchAccommodationResponse>> searchAccommodations(
            @RequestParam String keyword,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam int capacity,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        List<SearchAccommodationResponse> response = accommodationService.search(keyword, checkIn, checkOut, capacity, pageable);
        return ApiResponse.success(response);
    }
}
