package com.f1v3.reservation.supplier.accommodation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 숙소 생성 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record CreateAccommodationRequest(

        @NotBlank(message = "숙소 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "숙소 설명을 입력해주세요.")
        String description,

        @NotBlank(message = "숙소 주소를 입력해주세요.")
        String address,

        @NotBlank(message = "숙소 연락처를 입력해주세요.")
        String contactNumber
) {
}
