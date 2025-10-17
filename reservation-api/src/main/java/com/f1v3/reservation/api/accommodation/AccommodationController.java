package com.f1v3.reservation.api.accommodation;

import com.f1v3.reservation.api.accommodation.dto.SearchAccommodationResponse;
import com.f1v3.reservation.common.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * 숙소 검색 (키워드를 기반으로 name, region에 포함된 숙소 조회)
     */
    @GetMapping
    public ApiResponse<List<SearchAccommodationResponse>> searchAccommodations(@RequestParam String keyword) {
        List<SearchAccommodationResponse> response = accommodationService.search(keyword);
        return ApiResponse.success(response);
    }


}
