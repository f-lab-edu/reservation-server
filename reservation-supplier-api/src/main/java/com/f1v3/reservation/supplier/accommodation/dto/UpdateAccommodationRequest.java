package com.f1v3.reservation.supplier.accommodation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 숙소 수정 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record UpdateAccommodationRequest(

        @NotBlank(message = "숙소 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "숙소 설명을 입력해주세요.")
        String description,

        @NotBlank(message = "숙소 주소를 입력해주세요.")
        String address,

        @NotBlank(message = "숙소 연락처를 입력해주세요.")
        String contactNumber,

        @NotBlank(message = "썸네일 이미지를 입력해주세요.")
        @Pattern(regexp = "^https?://.*", message = "썸네일 이미지는 http 또는 https로 시작하는 URL 형식이어야 합니다.")
        String thumbnail
) {
}
