package com.f1v3.reservation.supplier.accommodation;

import com.f1v3.reservation.auth.web.user.Login;
import com.f1v3.reservation.auth.web.user.LoginUser;
import com.f1v3.reservation.common.api.response.ApiResponse;
import com.f1v3.reservation.supplier.accommodation.dto.AccommodationResponse;
import com.f1v3.reservation.supplier.accommodation.dto.CreateAccommodationRequest;
import com.f1v3.reservation.supplier.accommodation.dto.CreateAccommodationResponse;
import com.f1v3.reservation.supplier.accommodation.dto.UpdateAccommodationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공급자 숙소 관리 API
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/supplier/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateAccommodationResponse> createAccommodation(
            @Valid @RequestBody CreateAccommodationRequest request,
            @Login LoginUser user
    ) {
        return ApiResponse.success(accommodationService.create(request, user.id()));
    }

    @GetMapping
    public ApiResponse<List<AccommodationResponse>> findAccommodation(
            @Login LoginUser user
    ) {
        return ApiResponse.success(accommodationService.findAccommodation(user.id()));
    }

    @GetMapping("/{accommodationId}")
    public ApiResponse<AccommodationResponse> getDetailAccommodation(
            @PathVariable Long accommodationId,
            @Login LoginUser user
    ) {
        return ApiResponse.success(accommodationService.getDetailAccommodation(accommodationId, user.id()));
    }

    @PutMapping("/{accommodationId}")
    public void updateAccommodation(
            @PathVariable Long accommodationId,
            @Valid @RequestBody UpdateAccommodationRequest request,
            @Login LoginUser user
    ) {
        accommodationService.updateAccommodation(accommodationId, request, user.id());
    }

    @DeleteMapping("/{accommodationId}")
    public void deleteAccommodation(
            @PathVariable Long accommodationId,
            @Login LoginUser user
    ) {
        accommodationService.deleteAccommodation(accommodationId, user.id());
    }
}
